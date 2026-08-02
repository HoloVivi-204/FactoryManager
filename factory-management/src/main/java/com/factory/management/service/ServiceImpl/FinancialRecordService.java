package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.FinancialRecordRequest;
import com.factory.management.dto.request.FinancialPaymentRequest;
import com.factory.management.dto.response.FinancialPaymentResponse;
import com.factory.management.dto.response.FinancialRecordResponse;
import com.factory.management.dto.response.FinancialSummaryResponse;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.entity.Department;
import com.factory.management.entity.ExpenseGroup;
import com.factory.management.entity.Factory;
import com.factory.management.entity.FinancialCategory;
import com.factory.management.entity.FinancialEntryType;
import com.factory.management.entity.FinancialRecord;
import com.factory.management.entity.FinancialRecordStatus;
import com.factory.management.entity.FinancialPayment;
import com.factory.management.entity.PaymentStatus;
import com.factory.management.entity.ProductionLine;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.repository.DepartmentRepository;
import com.factory.management.repository.FactoryRepository;
import com.factory.management.repository.FinancialCategoryRepository;
import com.factory.management.repository.FinancialRecordRepository;
import com.factory.management.repository.FinancialPaymentRepository;
import com.factory.management.repository.ProductionLineRepository;
import com.factory.management.security.AuthorizationScope;
import com.factory.management.service.Service.CurrentUserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FinancialRecordService {
    private static final int MAX_PAGE_SIZE = 200;

    private final FinancialRecordRepository repository;
    private final FinancialPaymentRepository paymentRepository;
    private final FinancialCategoryRepository categories;
    private final FactoryRepository factories;
    private final DepartmentRepository departments;
    private final ProductionLineRepository lines;
    private final AuthorizationScope scope;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional
    public FinancialRecordResponse create(FinancialRecordRequest request) {
        FinancialRecord value = new FinancialRecord();
        value.setRecordNo(nextNo(request.getRecordDate()));
        value.setStatus(FinancialRecordStatus.DRAFT);
        apply(value, request);
        value.setCreatedBy(currentUserService.user().getUsername());
        value.setActive(request.getActive() == null || request.getActive());
        value = repository.save(value);
        auditService.record("CREATE", "FINANCIAL_RECORD", value.getId(),
                "{\"recordNo\":\"" + value.getRecordNo() + "\"}");
        return response(value);
    }

    @Transactional
    public FinancialRecordResponse update(Long id, FinancialRecordRequest request) {
        FinancialRecord value = find(id);
        check(value);
        requireDraft(value);
        apply(value, request);
        if (request.getActive() != null) value.setActive(request.getActive());
        auditService.record("UPDATE", "FINANCIAL_RECORD", value.getId(), null);
        return response(value);
    }

    @Transactional
    public void delete(Long id) {
        FinancialRecord value = find(id);
        check(value);
        requireDraft(value);
        value.setActive(false);
        auditService.record("DEACTIVATE", "FINANCIAL_RECORD", value.getId(), null);
    }

    @Transactional
    public FinancialRecordResponse post(Long id) {
        FinancialRecord value = findForUpdate(id);
        check(value);
        requireDraft(value);
        if (!Boolean.TRUE.equals(value.getActive())) {
            throw new AppException(ErrorCode.FINANCIAL_RECORD_INACTIVE);
        }
        value.setStatus(FinancialRecordStatus.POSTED);
        value.setPostedAt(LocalDateTime.now());
        value.setPostedBy(currentUserService.user().getUsername());
        if (value.getPaidAmount() != null && value.getPaidAmount().signum() > 0) {
            savePayment(value, value.getRecordDate(), value.getPaidAmount(), value.getReferenceNo(),
                    "Số tiền đã thanh toán khi ghi sổ", null);
        }
        auditService.record("POST", "FINANCIAL_RECORD", value.getId(),
                "{\"recordNo\":\"" + value.getRecordNo() + "\"}");
        return response(value);
    }

    @Transactional
    public FinancialRecordResponse voidRecord(Long id, String reason) {
        FinancialRecord value = findForUpdate(id);
        check(value);
        if (status(value) != FinancialRecordStatus.POSTED) {
            throw new AppException(ErrorCode.FINANCIAL_RECORD_NOT_POSTED);
        }
        if (reason == null || reason.isBlank()) {
            throw new AppException(ErrorCode.FINANCIAL_VOID_REASON_REQUIRED);
        }
        if (value.getPaidAmount() != null && value.getPaidAmount().signum() > 0) {
            throw new AppException(ErrorCode.FINANCIAL_RECORD_HAS_PAYMENTS);
        }
        value.setStatus(FinancialRecordStatus.VOIDED);
        value.setVoidedAt(LocalDateTime.now());
        value.setVoidedBy(currentUserService.user().getUsername());
        value.setVoidReason(reason.trim());
        auditService.record("VOID", "FINANCIAL_RECORD", value.getId(),
                "{\"reason\":\"" + safeJson(reason.trim()) + "\"}");
        return response(value);
    }

    @Transactional
    public FinancialPaymentResponse addPayment(Long recordId, FinancialPaymentRequest request) {
        FinancialRecord value = findForUpdate(recordId);
        check(value);
        if (status(value) != FinancialRecordStatus.POSTED) {
            throw new AppException(ErrorCode.FINANCIAL_RECORD_NOT_POSTED);
        }
        BigDecimal paid = value.getPaidAmount() == null ? BigDecimal.ZERO : value.getPaidAmount();
        BigDecimal outstanding = value.getAmount().subtract(paid);
        if (request.getAmount().compareTo(outstanding) > 0) {
            throw new AppException(ErrorCode.INVALID_PAID_AMOUNT);
        }
        FinancialPayment payment = savePayment(value, request.getPaymentDate(), request.getAmount(),
                trimToNull(request.getReferenceNo()), trimToNull(request.getNote()), null);
        updatePaidAmount(value, paid.add(request.getAmount()));
        auditService.record("ADD_PAYMENT", "FINANCIAL_RECORD", value.getId(),
                "{\"paymentId\":" + payment.getId() + ",\"amount\":" + request.getAmount() + "}");
        return paymentResponse(payment);
    }

    @Transactional
    public FinancialPaymentResponse reversePayment(Long recordId, Long paymentId, String reason) {
        FinancialRecord value = findForUpdate(recordId);
        check(value);
        FinancialPayment original = paymentRepository.findById(paymentId)
                .filter(payment -> payment.getFinancialRecord().getId().equals(recordId))
                .orElseThrow(() -> new AppException(ErrorCode.FINANCIAL_PAYMENT_NOT_FOUND));
        if (original.getReversalOf() != null || original.getReversedAt() != null || original.getAmount().signum() <= 0) {
            throw new AppException(ErrorCode.FINANCIAL_PAYMENT_ALREADY_REVERSED);
        }
        if (reason == null || reason.isBlank()) {
            throw new AppException(ErrorCode.FINANCIAL_PAYMENT_REVERSAL_REASON_REQUIRED);
        }
        BigDecimal paid = value.getPaidAmount() == null ? BigDecimal.ZERO : value.getPaidAmount();
        if (paid.compareTo(original.getAmount()) < 0) {
            throw new AppException(ErrorCode.INVALID_PAID_AMOUNT);
        }
        FinancialPayment reversal = savePayment(value, LocalDate.now(), original.getAmount().negate(),
                original.getPaymentNo(), "Đảo thanh toán " + original.getPaymentNo() + ": " + reason.trim(), original);
        original.setReversedAt(LocalDateTime.now());
        original.setReversedBy(currentUserService.user().getUsername());
        updatePaidAmount(value, paid.subtract(original.getAmount()));
        auditService.record("REVERSE_PAYMENT", "FINANCIAL_RECORD", value.getId(),
                "{\"paymentId\":" + original.getId() + ",\"reversalId\":" + reversal.getId() + "}");
        return paymentResponse(reversal);
    }

    @Transactional(readOnly = true)
    public List<FinancialPaymentResponse> payments(Long recordId) {
        FinancialRecord value = find(recordId);
        check(value);
        return paymentRepository.findAllByFinancialRecord_IdOrderByPaymentDateDescCreatedAtDesc(recordId)
                .stream().map(this::paymentResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<FinancialRecordResponse> search(
            LocalDate from,
            LocalDate to,
            Long factoryId,
            Long departmentId,
            Long lineId,
            FinancialEntryType type,
            FinancialRecordStatus status
    ) {
        validateRange(from, to);
        return repository.findAll(spec(from, to, factoryId, departmentId, lineId, type, status)).stream()
                .filter(this::hasScope)
                .sorted(Comparator.comparing(FinancialRecord::getRecordDate)
                        .thenComparing(FinancialRecord::getId).reversed())
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<FinancialRecordResponse> searchPage(
            LocalDate from,
            LocalDate to,
            Long factoryId,
            Long departmentId,
            Long lineId,
            FinancialEntryType type,
            FinancialRecordStatus status,
            int page,
            int size
    ) {
        List<FinancialRecordResponse> values = search(
                from, to, factoryId, departmentId, lineId, type, status);
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int fromIndex = Math.min(safePage * safeSize, values.size());
        int toIndex = Math.min(fromIndex + safeSize, values.size());
        int totalPages = values.isEmpty() ? 0 : (int) Math.ceil((double) values.size() / safeSize);
        return PageResponse.<FinancialRecordResponse>builder()
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
    public FinancialSummaryResponse summary(
            LocalDate from,
            LocalDate to,
            Long factoryId,
            Long departmentId,
            Long lineId
    ) {
        List<FinancialRecordResponse> values = search(
                from, to, factoryId, departmentId, lineId, null, FinancialRecordStatus.POSTED);
        BigDecimal revenue = sum(values, FinancialEntryType.REVENUE);
        BigDecimal expense = sum(values, FinancialEntryType.EXPENSE);
        BigDecimal receivable = values.stream()
                .filter(value -> value.getEntryType() == FinancialEntryType.REVENUE)
                .map(FinancialRecordResponse::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal payable = values.stream()
                .filter(value -> value.getEntryType() == FinancialEntryType.EXPENSE)
                .map(FinancialRecordResponse::getOutstandingAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> groups = new LinkedHashMap<>();
        for (ExpenseGroup group : ExpenseGroup.values()) {
            groups.put(group.name(), values.stream()
                    .filter(value -> value.getEntryType() == FinancialEntryType.EXPENSE
                            && value.getExpenseGroup() == group)
                    .map(FinancialRecordResponse::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add));
        }
        return FinancialSummaryResponse.builder()
                .fromDate(from)
                .toDate(to)
                .totalRevenue(revenue)
                .totalExpense(expense)
                .profit(revenue.subtract(expense))
                .accountsReceivable(receivable)
                .accountsPayable(payable)
                .expenseByGroup(groups)
                .recordCount(values.size())
                .build();
    }

    private Specification<FinancialRecord> spec(
            LocalDate from,
            LocalDate to,
            Long factoryId,
            Long departmentId,
            Long lineId,
            FinancialEntryType type,
            FinancialRecordStatus status
    ) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("recordDate"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("recordDate"), to));
            if (factoryId != null) predicates.add(cb.equal(root.get("factory").get("id"), factoryId));
            if (departmentId != null) predicates.add(cb.equal(root.get("department").get("id"), departmentId));
            if (lineId != null) predicates.add(cb.equal(root.get("productionLine").get("id"), lineId));
            if (type != null) predicates.add(cb.equal(root.get("category").get("entryType"), type));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private BigDecimal sum(List<FinancialRecordResponse> values, FinancialEntryType type) {
        return values.stream().filter(value -> value.getEntryType() == type)
                .map(FinancialRecordResponse::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void apply(FinancialRecord value, FinancialRecordRequest request) {
        if (!scope.canAccessFinancialScope(
                request.getFactoryId(), request.getDepartmentId(), request.getProductionLineId())) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        FinancialCategory category = categories.findByIdAndActiveTrue(request.getCategoryId())
                .orElseThrow(() -> new AppException(ErrorCode.FINANCIAL_CATEGORY_NOT_FOUND));
        Factory factory = factories.findByIdAndActiveTrue(request.getFactoryId())
                .orElseThrow(() -> new AppException(ErrorCode.FACTORY_ID_NOT_FOUND));
        Department department = request.getDepartmentId() == null ? null
                : departments.findActiveByIdInActiveHierarchy(request.getDepartmentId())
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_ID_NOT_FOUND));
        ProductionLine line = request.getProductionLineId() == null ? null
                : lines.findActiveByIdInActiveHierarchy(request.getProductionLineId())
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_LINE_ID_NOT_FOUND));
        if ((department != null && !department.getFactory().getId().equals(factory.getId()))
                || (line != null && (department == null
                || !line.getDepartment().getId().equals(department.getId())))) {
            throw new AppException(ErrorCode.INVALID_FINANCIAL_HIERARCHY);
        }
        BigDecimal paid = request.getPaidAmount() == null ? BigDecimal.ZERO : request.getPaidAmount();
        if (paid.compareTo(request.getAmount()) > 0) {
            throw new AppException(ErrorCode.INVALID_PAID_AMOUNT);
        }
        value.setRecordDate(request.getRecordDate());
        value.setCategory(category);
        value.setFactory(factory);
        value.setDepartment(department);
        value.setProductionLine(line);
        value.setAmount(request.getAmount());
        value.setPaidAmount(paid);
        value.setDueDate(request.getDueDate());
        value.setCounterparty(trimToNull(request.getCounterparty()));
        value.setPaymentStatus(paid.signum() == 0
                ? PaymentStatus.UNPAID
                : paid.compareTo(request.getAmount()) == 0 ? PaymentStatus.PAID : PaymentStatus.PARTIAL);
        value.setReferenceNo(trimToNull(request.getReferenceNo()));
        value.setDescription(trimToNull(request.getDescription()));
    }

    private void requireDraft(FinancialRecord value) {
        if (status(value) != FinancialRecordStatus.DRAFT) {
            throw new AppException(ErrorCode.FINANCIAL_RECORD_LOCKED);
        }
    }

    private FinancialRecordStatus status(FinancialRecord value) {
        return value.getStatus() == null ? FinancialRecordStatus.DRAFT : value.getStatus();
    }

    private void check(FinancialRecord value) {
        if (!hasScope(value)) throw new AppException(ErrorCode.ACCESS_DENIED);
    }

    private boolean hasScope(FinancialRecord value) {
        return scope.canAccessFinancialScope(
                value.getFactory().getId(),
                value.getDepartment() == null ? null : value.getDepartment().getId(),
                value.getProductionLine() == null ? null : value.getProductionLine().getId());
    }

    private FinancialRecord find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FINANCIAL_RECORD_NOT_FOUND));
    }

    private FinancialRecord findForUpdate(Long id) {
        return repository.findForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.FINANCIAL_RECORD_NOT_FOUND));
    }

    private void validateRange(LocalDate from, LocalDate to) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new AppException(ErrorCode.INVALID_FINANCIAL_DATE_RANGE);
        }
    }

    private String nextNo(LocalDate date) {
        return "FIN" + date.format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private FinancialRecordResponse response(FinancialRecord value) {
        BigDecimal paid = value.getPaidAmount() == null ? BigDecimal.ZERO : value.getPaidAmount();
        return FinancialRecordResponse.builder()
                .id(value.getId())
                .recordNo(value.getRecordNo())
                .recordDate(value.getRecordDate())
                .categoryId(value.getCategory().getId())
                .categoryCode(value.getCategory().getCode())
                .categoryName(value.getCategory().getName())
                .entryType(value.getCategory().getEntryType())
                .expenseGroup(value.getCategory().getExpenseGroup())
                .factoryId(value.getFactory().getId())
                .factoryName(value.getFactory().getName())
                .departmentId(value.getDepartment() == null ? null : value.getDepartment().getId())
                .departmentName(value.getDepartment() == null ? null : value.getDepartment().getName())
                .productionLineId(value.getProductionLine() == null ? null : value.getProductionLine().getId())
                .productionLineName(value.getProductionLine() == null ? null : value.getProductionLine().getName())
                .amount(value.getAmount())
                .paidAmount(paid)
                .outstandingAmount(value.getAmount().subtract(paid))
                .dueDate(value.getDueDate())
                .counterparty(value.getCounterparty())
                .paymentStatus(value.getPaymentStatus())
                .referenceNo(value.getReferenceNo())
                .description(value.getDescription())
                .status(status(value))
                .postedAt(value.getPostedAt())
                .postedBy(value.getPostedBy())
                .voidedAt(value.getVoidedAt())
                .voidedBy(value.getVoidedBy())
                .voidReason(value.getVoidReason())
                .active(value.getActive())
                .createdBy(value.getCreatedBy())
                .createdAt(value.getCreatedAt())
                .version(value.getVersion())
                .build();
    }

    private FinancialPayment savePayment(
            FinancialRecord record,
            LocalDate paymentDate,
            BigDecimal amount,
            String referenceNo,
            String note,
            FinancialPayment reversalOf
    ) {
        return paymentRepository.save(FinancialPayment.builder()
                .paymentNo("PAY" + paymentDate.format(DateTimeFormatter.BASIC_ISO_DATE) + "-"
                        + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                .financialRecord(record)
                .paymentDate(paymentDate)
                .amount(amount)
                .referenceNo(referenceNo)
                .note(note)
                .reversalOf(reversalOf)
                .createdBy(currentUserService.user().getUsername())
                .build());
    }

    private void updatePaidAmount(FinancialRecord record, BigDecimal paid) {
        record.setPaidAmount(paid);
        record.setPaymentStatus(paid.signum() == 0
                ? PaymentStatus.UNPAID
                : paid.compareTo(record.getAmount()) == 0 ? PaymentStatus.PAID : PaymentStatus.PARTIAL);
    }

    private FinancialPaymentResponse paymentResponse(FinancialPayment value) {
        return FinancialPaymentResponse.builder()
                .id(value.getId())
                .paymentNo(value.getPaymentNo())
                .financialRecordId(value.getFinancialRecord().getId())
                .financialRecordNo(value.getFinancialRecord().getRecordNo())
                .paymentDate(value.getPaymentDate())
                .amount(value.getAmount())
                .referenceNo(value.getReferenceNo())
                .note(value.getNote())
                .reversalOfId(value.getReversalOf() == null ? null : value.getReversalOf().getId())
                .reversedAt(value.getReversedAt())
                .reversedBy(value.getReversedBy())
                .createdBy(value.getCreatedBy())
                .createdAt(value.getCreatedAt())
                .build();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String safeJson(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
