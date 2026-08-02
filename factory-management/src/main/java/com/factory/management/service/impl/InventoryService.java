package com.factory.management.service.impl;

import com.factory.management.dto.request.InventoryTransactionRequest;
import com.factory.management.dto.response.InventoryStockResponse;
import com.factory.management.dto.response.InventoryTransactionResponse;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.entity.InventoryBalance;
import com.factory.management.entity.InventoryTransaction;
import com.factory.management.entity.InventoryTransactionType;
import com.factory.management.entity.Material;
import com.factory.management.entity.Warehouse;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.repository.InventoryBalanceRepository;
import com.factory.management.repository.InventoryTransactionRepository;
import com.factory.management.repository.MaterialRepository;
import com.factory.management.repository.WarehouseRepository;
import com.factory.management.security.AuthorizationScope;
import com.factory.management.service.CurrentUserService;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class InventoryService {
    private static final int MAX_PAGE_SIZE = 200;

    private final InventoryTransactionRepository repository;
    private final InventoryBalanceRepository balanceRepository;
    private final WarehouseRepository warehouses;
    private final MaterialRepository materials;
    private final AuthorizationScope scope;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional
    public InventoryTransactionResponse create(InventoryTransactionRequest request) {
        Warehouse warehouse = lockedWarehouse(request.getWarehouseId());
        checkScope(warehouse);
        Material material = materials.findByIdAndActiveTrue(request.getMaterialId())
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_ID_NOT_FOUND));
        InventoryBalance balance = lockedBalance(warehouse, material);
        BigDecimal unitCost = effectiveUnitCost(request, balance);
        applyBalance(balance, request.getTransactionType(), request.getQuantity(), unitCost);

        InventoryTransaction value = repository.save(InventoryTransaction.builder()
                .transactionNo(nextNo(request.getTransactionDate()))
                .transactionDate(request.getTransactionDate())
                .transactionType(request.getTransactionType())
                .warehouse(warehouse)
                .material(material)
                .quantity(request.getQuantity())
                .unitCost(unitCost)
                .referenceNo(trimToNull(request.getReferenceNo()))
                .description(trimToNull(request.getDescription()))
                .active(true)
                .createdBy(currentUserService.user().getUsername())
                .build());
        auditService.record("CREATE", "INVENTORY_TRANSACTION", value.getId(),
                "{\"transactionNo\":\"" + value.getTransactionNo() + "\",\"warehouseId\":"
                        + warehouse.getId() + ",\"materialId\":" + material.getId() + "}");
        return response(value);
    }

    /**
     * Inventory movements are immutable. A correction creates an opposite
     * movement and retains both records for audit/reconciliation.
     */
    @Transactional
    public InventoryTransactionResponse reverse(Long id, String reason) {
        InventoryTransaction original = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_TRANSACTION_NOT_FOUND));
        Warehouse warehouse = lockedWarehouse(original.getWarehouse().getId());
        checkScope(warehouse);
        original = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVENTORY_TRANSACTION_NOT_FOUND));
        if (original.getReversalOf() != null || original.getReversedAt() != null) {
            throw new AppException(ErrorCode.INVENTORY_TRANSACTION_ALREADY_REVERSED);
        }
        if (reason == null || reason.isBlank()) {
            throw new AppException(ErrorCode.INVENTORY_REVERSAL_REASON_REQUIRED);
        }

        InventoryBalance balance = lockedBalance(warehouse, original.getMaterial());
        InventoryTransactionType reverseType = opposite(original.getTransactionType());
        applyBalance(balance, reverseType, original.getQuantity(), zero(original.getUnitCost()));
        String username = currentUserService.user().getUsername();
        InventoryTransaction reversal = repository.save(InventoryTransaction.builder()
                .transactionNo(nextNo(LocalDate.now()))
                .transactionDate(LocalDate.now())
                .transactionType(reverseType)
                .warehouse(warehouse)
                .material(original.getMaterial())
                .quantity(original.getQuantity())
                .unitCost(zero(original.getUnitCost()))
                .referenceNo(original.getTransactionNo())
                .description("Đảo giao dịch " + original.getTransactionNo() + ": " + reason.trim())
                .reversalOf(original)
                .active(true)
                .createdBy(username)
                .build());
        original.setReversedAt(LocalDateTime.now());
        original.setReversedBy(username);
        auditService.record("REVERSE", "INVENTORY_TRANSACTION", original.getId(),
                "{\"reversalId\":" + reversal.getId() + ",\"reason\":\"" + safeJson(reason.trim()) + "\"}");
        return response(reversal);
    }

    @Transactional(readOnly = true)
    public List<InventoryTransactionResponse> search(
            LocalDate from,
            LocalDate to,
            Long warehouseId,
            Long materialId,
            InventoryTransactionType type
    ) {
        validateRange(from, to);
        return repository.findAll(spec(from, to, warehouseId, materialId, type)).stream()
                .filter(value -> hasScope(value.getWarehouse()))
                .sorted(Comparator.comparing(InventoryTransaction::getCreatedAt).reversed())
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<InventoryTransactionResponse> searchPage(
            LocalDate from,
            LocalDate to,
            Long warehouseId,
            Long materialId,
            InventoryTransactionType type,
            int page,
            int size
    ) {
        List<InventoryTransactionResponse> values = search(from, to, warehouseId, materialId, type);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int fromIndex = Math.min(safePage * safeSize, values.size());
        int toIndex = Math.min(fromIndex + safeSize, values.size());
        int totalPages = values.isEmpty() ? 0 : (int) Math.ceil((double) values.size() / safeSize);
        return PageResponse.<InventoryTransactionResponse>builder()
                .content(values.subList(fromIndex, toIndex))
                .page(safePage)
                .size(safeSize)
                .totalElements(values.size())
                .totalPages(totalPages)
                .first(safePage == 0)
                .last(safePage >= Math.max(totalPages - 1, 0))
                .build();
    }

    @Transactional(readOnly = true)
    public List<InventoryStockResponse> stocks(Long warehouseId) {
        List<InventoryTransactionResponse> transactions = search(null, null, warehouseId, null, null);
        record Key(Long warehouseId, Long materialId) {}
        Map<Key, List<InventoryTransactionResponse>> grouped = new LinkedHashMap<>();
        transactions.forEach(value -> grouped.computeIfAbsent(
                new Key(value.getWarehouseId(), value.getMaterialId()), ignored -> new ArrayList<>()).add(value));
        return grouped.values().stream().map(values -> {
            InventoryTransactionResponse first = values.get(0);
            BigDecimal quantity = values.stream()
                    .map(value -> signed(value.getTransactionType(), value.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal inventoryValue = values.stream()
                    .map(value -> signed(value.getTransactionType(), value.getQuantity())
                            .multiply(zero(value.getUnitCost())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (quantity.signum() == 0) inventoryValue = BigDecimal.ZERO;
            BigDecimal average = quantity.signum() == 0
                    ? BigDecimal.ZERO
                    : inventoryValue.divide(quantity, 2, RoundingMode.HALF_UP);
            return InventoryStockResponse.builder()
                    .warehouseId(first.getWarehouseId())
                    .warehouseCode(first.getWarehouseCode())
                    .warehouseName(first.getWarehouseName())
                    .materialId(first.getMaterialId())
                    .materialCode(first.getMaterialCode())
                    .materialName(first.getMaterialName())
                    .unit(first.getUnit())
                    .quantityOnHand(quantity)
                    .averageUnitCost(average)
                    .inventoryValue(inventoryValue)
                    .build();
        }).filter(value -> value.getQuantityOnHand().signum() != 0 || value.getInventoryValue().signum() != 0).toList();
    }

    private Specification<InventoryTransaction> spec(
            LocalDate from,
            LocalDate to,
            Long warehouseId,
            Long materialId,
            InventoryTransactionType type
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), to));
            if (warehouseId != null) predicates.add(cb.equal(root.get("warehouse").get("id"), warehouseId));
            if (materialId != null) predicates.add(cb.equal(root.get("material").get("id"), materialId));
            if (type != null) predicates.add(cb.equal(root.get("transactionType"), type));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Warehouse lockedWarehouse(Long id) {
        return warehouses.findActiveForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.WAREHOUSE_NOT_FOUND));
    }

    private InventoryBalance lockedBalance(Warehouse warehouse, Material material) {
        return balanceRepository.findForUpdate(warehouse.getId(), material.getId())
                .orElseGet(() -> initializeBalance(warehouse, material));
    }

    private InventoryBalance initializeBalance(Warehouse warehouse, Material material) {
        BigDecimal quantity = BigDecimal.ZERO;
        BigDecimal value = BigDecimal.ZERO;
        for (InventoryTransaction transaction : repository
                .findAllByWarehouse_IdAndMaterial_IdAndActiveTrue(warehouse.getId(), material.getId())) {
            BigDecimal signedQuantity = signed(transaction.getTransactionType(), transaction.getQuantity());
            quantity = quantity.add(signedQuantity);
            value = value.add(signedQuantity.multiply(zero(transaction.getUnitCost())));
        }
        if (quantity.signum() == 0) value = BigDecimal.ZERO;
        return balanceRepository.save(InventoryBalance.builder()
                .warehouse(warehouse)
                .material(material)
                .quantityOnHand(quantity)
                .inventoryValue(value)
                .build());
    }

    private BigDecimal effectiveUnitCost(InventoryTransactionRequest request, InventoryBalance balance) {
        if (!out(request.getTransactionType())) {
            if (request.getUnitCost() == null) {
                throw new AppException(ErrorCode.INVENTORY_UNIT_COST_REQUIRED);
            }
            return request.getUnitCost();
        }
        if (balance.getQuantityOnHand().signum() <= 0) {
            throw new AppException(ErrorCode.INSUFFICIENT_INVENTORY);
        }
        return balance.getInventoryValue().divide(balance.getQuantityOnHand(), 2, RoundingMode.HALF_UP);
    }

    private void applyBalance(
            InventoryBalance balance,
            InventoryTransactionType type,
            BigDecimal quantity,
            BigDecimal unitCost
    ) {
        BigDecimal signedQuantity = signed(type, quantity);
        BigDecimal nextQuantity = balance.getQuantityOnHand().add(signedQuantity);
        if (nextQuantity.signum() < 0) throw new AppException(ErrorCode.INSUFFICIENT_INVENTORY);
        BigDecimal nextValue = balance.getInventoryValue().add(signedQuantity.multiply(unitCost));
        if (nextQuantity.signum() == 0) nextValue = BigDecimal.ZERO;
        if (nextValue.signum() < 0) nextValue = BigDecimal.ZERO;
        balance.setQuantityOnHand(nextQuantity);
        balance.setInventoryValue(nextValue);
    }

    private void checkScope(Warehouse warehouse) {
        if (!hasScope(warehouse)) throw new AppException(ErrorCode.ACCESS_DENIED);
    }

    private boolean hasScope(Warehouse warehouse) {
        return scope.canAccessFinancialScope(warehouse.getFactory().getId(), null, null);
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new AppException(ErrorCode.INVALID_INVENTORY_DATE_RANGE);
        }
    }

    private boolean out(InventoryTransactionType type) {
        return type == InventoryTransactionType.OUTBOUND || type == InventoryTransactionType.ADJUSTMENT_OUT;
    }

    private InventoryTransactionType opposite(InventoryTransactionType type) {
        return switch (type) {
            case INBOUND -> InventoryTransactionType.ADJUSTMENT_OUT;
            case OUTBOUND -> InventoryTransactionType.ADJUSTMENT_IN;
            case ADJUSTMENT_IN -> InventoryTransactionType.ADJUSTMENT_OUT;
            case ADJUSTMENT_OUT -> InventoryTransactionType.ADJUSTMENT_IN;
        };
    }

    private BigDecimal signed(InventoryTransactionType type, BigDecimal quantity) {
        return out(type) ? quantity.negate() : quantity;
    }

    private String nextNo(LocalDate date) {
        return "INV" + date.format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private InventoryTransactionResponse response(InventoryTransaction value) {
        return InventoryTransactionResponse.builder()
                .id(value.getId())
                .transactionNo(value.getTransactionNo())
                .transactionDate(value.getTransactionDate())
                .transactionType(value.getTransactionType())
                .warehouseId(value.getWarehouse().getId())
                .warehouseCode(value.getWarehouse().getCode())
                .warehouseName(value.getWarehouse().getName())
                .materialId(value.getMaterial().getId())
                .materialCode(value.getMaterial().getCode())
                .materialName(value.getMaterial().getName())
                .unit(value.getMaterial().getUnit())
                .quantity(value.getQuantity())
                .unitCost(value.getUnitCost())
                .totalAmount(value.getQuantity().multiply(zero(value.getUnitCost())))
                .referenceNo(value.getReferenceNo())
                .description(value.getDescription())
                .reversalOfId(value.getReversalOf() == null ? null : value.getReversalOf().getId())
                .reversedAt(value.getReversedAt())
                .reversedBy(value.getReversedBy())
                .active(value.getActive())
                .createdBy(value.getCreatedBy())
                .createdAt(value.getCreatedAt())
                .build();
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
