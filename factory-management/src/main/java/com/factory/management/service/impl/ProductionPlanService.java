package com.factory.management.service.impl;

import com.factory.management.dto.request.ProductionPlanRequest;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.dto.response.ProductionPlanResponse;
import com.factory.management.entity.Factory;
import com.factory.management.entity.Product;
import com.factory.management.entity.ProductionLine;
import com.factory.management.entity.ProductionOrderStatus;
import com.factory.management.entity.ProductionPlan;
import com.factory.management.entity.ProductionPlanStatus;
import com.factory.management.entity.Role;
import com.factory.management.entity.Team;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.repository.FactoryRepository;
import com.factory.management.repository.ProductionLineRepository;
import com.factory.management.repository.ProductionOrderRepository;
import com.factory.management.repository.ProductionPlanRepository;
import com.factory.management.repository.ProductRepository;
import com.factory.management.repository.TeamRepository;
import com.factory.management.security.AuthorizationScope;
import com.factory.management.service.CurrentUserService;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductionPlanService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final DateTimeFormatter NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final Set<ProductionOrderStatus> TERMINAL_ORDER_STATUSES =
            EnumSet.of(ProductionOrderStatus.COMPLETED, ProductionOrderStatus.CANCELLED);

    private final ProductionPlanRepository planRepository;
    private final ProductionOrderRepository orderRepository;
    private final FactoryRepository factoryRepository;
    private final ProductionLineRepository productionLineRepository;
    private final ProductRepository productRepository;
    private final TeamRepository teamRepository;
    private final AuthorizationScope authorizationScope;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<ProductionPlanResponse> search(
            LocalDate fromDate,
            LocalDate toDate,
            Long factoryId,
            Long productionLineId,
            Long productId,
            ProductionPlanStatus status,
            Boolean active,
            int page,
            int size
    ) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new AppException(ErrorCode.PRODUCTION_PLAN_INVALID_PERIOD);
        }
        Set<Long> visibleLineIds = visibleLineIds();
        Specification<ProductionPlan> specification = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (!hasGlobalRead()) {
                if (visibleLineIds.isEmpty()) return cb.disjunction();
                predicates.add(root.get("productionLine").get("id").in(visibleLineIds));
            }
            if (fromDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("periodEnd"), fromDate));
            if (toDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("periodStart"), toDate));
            if (factoryId != null) predicates.add(cb.equal(root.get("factory").get("id"), factoryId));
            if (productionLineId != null) {
                predicates.add(cb.equal(root.get("productionLine").get("id"), productionLineId));
            }
            if (productId != null) predicates.add(cb.equal(root.get("product").get("id"), productId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (active != null) predicates.add(cb.equal(root.get("active"), active));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        var result = planRepository.findAll(specification,
                PageRequest.of(safePage(page), safeSize(size),
                        Sort.by(Sort.Direction.DESC, "periodStart").and(Sort.by(Sort.Direction.DESC, "id"))));
        return PageResponse.from(result, this::response);
    }

    @Transactional(readOnly = true)
    public ProductionPlanResponse get(Long id) {
        ProductionPlan value = find(id);
        requireLineAccess(value.getProductionLine().getId());
        return response(value);
    }

    @Transactional
    public ProductionPlanResponse create(ProductionPlanRequest request) {
        validatePeriod(request.getPeriodStart(), request.getPeriodEnd());
        Factory factory = activeFactory(request.getFactoryId());
        ProductionLine line = activeLine(request.getProductionLineId());
        requireLineAccess(line.getId());
        requireFactoryLine(factory, line);
        Product product = activeProduct(request.getProductId());
        ensureUniqueBusinessKey(null, request);

        ProductionPlan value = planRepository.save(ProductionPlan.builder()
                .planNo(uniquePlanNo())
                .periodStart(request.getPeriodStart())
                .periodEnd(request.getPeriodEnd())
                .factory(factory)
                .productionLine(line)
                .product(product)
                .plannedQuantity(request.getPlannedQuantity())
                .status(ProductionPlanStatus.DRAFT)
                .note(trimToNull(request.getNote()))
                .active(true)
                .createdBy(currentUserService.user().getUsername())
                .build());
        auditService.record("CREATE", "PRODUCTION_PLAN", value.getId(),
                "{\"planNo\":\"" + value.getPlanNo() + "\"}");
        return response(value);
    }

    @Transactional
    public ProductionPlanResponse update(Long id, ProductionPlanRequest request) {
        ProductionPlan value = findForUpdate(id);
        requireLineAccess(value.getProductionLine().getId());
        requireDraft(value);
        validatePeriod(request.getPeriodStart(), request.getPeriodEnd());

        Factory factory = activeFactory(request.getFactoryId());
        ProductionLine line = activeLine(request.getProductionLineId());
        requireLineAccess(line.getId());
        requireFactoryLine(factory, line);
        Product product = activeProduct(request.getProductId());
        ensureUniqueBusinessKey(id, request);

        boolean hasOrders = orderRepository.existsByProductionPlan_IdAndActiveTrue(id);
        boolean structureChanged = !value.getFactory().getId().equals(factory.getId())
                || !value.getProductionLine().getId().equals(line.getId())
                || !value.getProduct().getId().equals(product.getId())
                || !value.getPeriodStart().equals(request.getPeriodStart())
                || !value.getPeriodEnd().equals(request.getPeriodEnd());
        if (hasOrders && structureChanged) {
            throw new AppException(ErrorCode.PRODUCTION_PLAN_STRUCTURE_LOCKED);
        }
        BigDecimal allocated = allocatedQuantity(id, null);
        if (request.getPlannedQuantity().compareTo(allocated) < 0) {
            throw new AppException(ErrorCode.PRODUCTION_PLAN_QUANTITY_BELOW_ALLOCATED);
        }

        value.setPeriodStart(request.getPeriodStart());
        value.setPeriodEnd(request.getPeriodEnd());
        value.setFactory(factory);
        value.setProductionLine(line);
        value.setProduct(product);
        value.setPlannedQuantity(request.getPlannedQuantity());
        value.setNote(trimToNull(request.getNote()));
        auditService.record("UPDATE", "PRODUCTION_PLAN", value.getId(), null);
        return response(value);
    }

    @Transactional
    public ProductionPlanResponse approve(Long id) {
        ProductionPlan value = findForUpdate(id);
        requireLineAccess(value.getProductionLine().getId());
        requireDraft(value);
        requireCurrentMasterData(value);
        value.setStatus(ProductionPlanStatus.APPROVED);
        value.setApprovedAt(LocalDateTime.now());
        value.setApprovedBy(currentUserService.user().getUsername());
        auditService.record("APPROVE", "PRODUCTION_PLAN", value.getId(), null);
        return response(value);
    }

    @Transactional
    public ProductionPlanResponse close(Long id) {
        ProductionPlan value = findForUpdate(id);
        requireLineAccess(value.getProductionLine().getId());
        if (value.getStatus() != ProductionPlanStatus.APPROVED) {
            throw new AppException(ErrorCode.INVALID_PRODUCTION_PLAN_TRANSITION);
        }
        if (!orderRepository.existsByProductionPlan_IdAndActiveTrue(id)) {
            throw new AppException(ErrorCode.PRODUCTION_PLAN_HAS_NO_ORDERS);
        }
        if (orderRepository.existsByProductionPlan_IdAndActiveTrueAndStatusNotIn(id, TERMINAL_ORDER_STATUSES)) {
            throw new AppException(ErrorCode.PRODUCTION_PLAN_HAS_OPEN_ORDERS);
        }
        value.setStatus(ProductionPlanStatus.CLOSED);
        value.setClosedAt(LocalDateTime.now());
        value.setClosedBy(currentUserService.user().getUsername());
        auditService.record("CLOSE", "PRODUCTION_PLAN", value.getId(), null);
        return response(value);
    }

    @Transactional
    public void delete(Long id) {
        ProductionPlan value = findForUpdate(id);
        requireLineAccess(value.getProductionLine().getId());
        requireDraft(value);
        if (orderRepository.existsByProductionPlan_IdAndActiveTrue(id)) {
            throw new AppException(ErrorCode.PRODUCTION_PLAN_HAS_ORDERS);
        }
        value.setActive(false);
        auditService.record("SOFT_DELETE", "PRODUCTION_PLAN", value.getId(), null);
    }

    private void validatePeriod(LocalDate start, LocalDate end) {
        if (start.isAfter(end)) throw new AppException(ErrorCode.PRODUCTION_PLAN_INVALID_PERIOD);
    }

    private Factory activeFactory(Long id) {
        return factoryRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.FACTORY_ID_NOT_FOUND));
    }

    private ProductionLine activeLine(Long id) {
        return productionLineRepository
                .findActiveByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_LINE_ID_NOT_FOUND));
    }

    private Product activeProduct(Long id) {
        return productRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private ProductionPlan find(Long id) {
        return planRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_PLAN_NOT_FOUND));
    }

    private ProductionPlan findForUpdate(Long id) {
        return planRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_PLAN_NOT_FOUND));
    }

    private void requireFactoryLine(Factory factory, ProductionLine line) {
        if (!line.getDepartment().getFactory().getId().equals(factory.getId())) {
            throw new AppException(ErrorCode.PRODUCTION_PLAN_INVALID_HIERARCHY);
        }
    }

    private void requireCurrentMasterData(ProductionPlan value) {
        if (!Boolean.TRUE.equals(value.getActive())
                || !Boolean.TRUE.equals(value.getFactory().getActive())
                || !Boolean.TRUE.equals(value.getProductionLine().getActive())
                || !Boolean.TRUE.equals(value.getProduct().getActive())) {
            throw new AppException(ErrorCode.PRODUCTION_PLAN_INACTIVE_MASTER_DATA);
        }
    }

    private void requireDraft(ProductionPlan value) {
        if (!Boolean.TRUE.equals(value.getActive()) || value.getStatus() != ProductionPlanStatus.DRAFT) {
            throw new AppException(ErrorCode.PRODUCTION_PLAN_NOT_DRAFT);
        }
    }

    private void ensureUniqueBusinessKey(Long id, ProductionPlanRequest request) {
        boolean exists = id == null
                ? planRepository.existsByFactory_IdAndProductionLine_IdAndProduct_IdAndPeriodStartAndPeriodEndAndActiveTrue(
                        request.getFactoryId(), request.getProductionLineId(), request.getProductId(),
                        request.getPeriodStart(), request.getPeriodEnd())
                : planRepository.existsByFactory_IdAndProductionLine_IdAndProduct_IdAndPeriodStartAndPeriodEndAndActiveTrueAndIdNot(
                        request.getFactoryId(), request.getProductionLineId(), request.getProductId(),
                        request.getPeriodStart(), request.getPeriodEnd(), id);
        if (exists) throw new AppException(ErrorCode.PRODUCTION_PLAN_EXISTS);
    }

    private void requireLineAccess(Long lineId) {
        if (hasGlobalRead()) return;
        if (!visibleLineIds().contains(lineId)) throw new AppException(ErrorCode.ACCESS_DENIED);
    }

    private Set<Long> visibleLineIds() {
        Set<Long> teamIds = authorizationScope.accessibleTeamIds();
        if (teamIds.isEmpty()) return Set.of();
        return teamRepository.findAllById(teamIds).stream()
                .map(Team::getProductionLine)
                .map(ProductionLine::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    private boolean hasGlobalRead() {
        Set<Role> roles = authorizationScope.currentRoles();
        return roles.contains(Role.ADMIN) || roles.contains(Role.DIRECTOR);
    }

    private String uniquePlanNo() {
        String value;
        do {
            value = "PP" + LocalDate.now().format(NUMBER_DATE) + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        } while (planRepository.existsByPlanNo(value));
        return value;
    }

    private BigDecimal allocatedQuantity(Long planId, Long excludeOrderId) {
        BigDecimal value = orderRepository.sumActivePlannedQuantity(planId, excludeOrderId);
        return value == null ? BigDecimal.ZERO : value;
    }

    private ProductionPlanResponse response(ProductionPlan value) {
        return ProductionPlanResponse.builder()
                .id(value.getId())
                .planNo(value.getPlanNo())
                .periodStart(value.getPeriodStart())
                .periodEnd(value.getPeriodEnd())
                .factoryId(value.getFactory().getId())
                .factoryCode(value.getFactory().getCode())
                .factoryName(value.getFactory().getName())
                .productionLineId(value.getProductionLine().getId())
                .productionLineCode(value.getProductionLine().getCode())
                .productionLineName(value.getProductionLine().getName())
                .productId(value.getProduct().getId())
                .productCode(value.getProduct().getCode())
                .productName(value.getProduct().getName())
                .productUnit(value.getProduct().getUnit())
                .plannedQuantity(value.getPlannedQuantity())
                .allocatedQuantity(allocatedQuantity(value.getId(), null))
                .status(value.getStatus())
                .note(value.getNote())
                .active(value.getActive())
                .approvedAt(value.getApprovedAt())
                .approvedBy(value.getApprovedBy())
                .closedAt(value.getClosedAt())
                .closedBy(value.getClosedBy())
                .createdBy(value.getCreatedBy())
                .createdAt(value.getCreatedAt())
                .updatedAt(value.getUpdatedAt())
                .version(value.getVersion())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private int safePage(int value) {
        return Math.max(value, 0);
    }

    private int safeSize(int value) {
        return Math.min(Math.max(value, 1), MAX_PAGE_SIZE);
    }
}
