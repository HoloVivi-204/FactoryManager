package com.factory.management.service.impl;

import com.factory.management.dto.response.ExecutiveDashboardResponse;
import com.factory.management.entity.FinancialEntryType;
import com.factory.management.entity.FinancialRecord;
import com.factory.management.entity.FinancialRecordStatus;
import com.factory.management.entity.MaintenanceSchedule;
import com.factory.management.entity.MaintenanceWorkOrder;
import com.factory.management.entity.MaintenanceWorkOrderStatus;
import com.factory.management.entity.PaymentStatus;
import com.factory.management.entity.ProductionReport;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.repository.FactoryRepository;
import com.factory.management.repository.FinancialRecordRepository;
import com.factory.management.repository.MaintenanceScheduleRepository;
import com.factory.management.repository.MaintenanceWorkOrderRepository;
import com.factory.management.repository.ProductionReportRepository;
import jakarta.persistence.criteria.JoinType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ExecutiveDashboardService {
    private static final int MAX_RANGE_DAYS = 731;
    private static final int DAILY_TREND_MAX_DAYS = 62;
    private static final BigDecimal MIN_PLAN_ATTAINMENT = new BigDecimal("90.00");
    private static final BigDecimal MAX_DEFECT_RATE = new BigDecimal("3.00");
    private static final BigDecimal MIN_OEE = new BigDecimal("75.00");
    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final FactoryRepository factoryRepository;
    private final FinancialRecordRepository financialRecordRepository;
    private final ProductionReportRepository productionReportRepository;
    private final MaintenanceScheduleRepository maintenanceScheduleRepository;
    private final MaintenanceWorkOrderRepository maintenanceWorkOrderRepository;

    @Transactional(readOnly = true)
    public ExecutiveDashboardResponse getDashboard(
            LocalDate requestedFromDate,
            LocalDate requestedToDate,
            Long factoryId
    ) {
        DateRange range = resolveRange(requestedFromDate, requestedToDate);
        String factoryName = validateAndGetFactoryName(factoryId);
        LocalDate asOfDate = range.toDate().isAfter(LocalDate.now()) ? LocalDate.now() : range.toDate();

        List<FinancialRecord> financialRecords = financialRecordRepository.findAll(
                financialPeriodSpec(range.fromDate(), range.toDate(), factoryId));
        List<ProductionReport> productionReports = productionReportRepository.findAll(
                productionPeriodSpec(range.fromDate(), range.toDate(), factoryId));
        List<FinancialRecord> overdueRecords = financialRecordRepository.findAll(
                overdueFinancialSpec(asOfDate, factoryId));

        Aggregate total = new Aggregate();
        Map<String, PeriodAggregate> periods = initializePeriods(range);
        Map<Long, FactoryAggregate> factories = new LinkedHashMap<>();

        for (FinancialRecord record : financialRecords) {
            total.add(record);
            periodFor(periods, record.getRecordDate(), range.granularity()).add(record);
            factoryFor(factories, record.getFactory().getId(), record.getFactory().getName()).add(record);
        }
        for (ProductionReport report : productionReports) {
            total.add(report);
            periodFor(periods, report.getReportDate(), range.granularity()).add(report);
            factoryFor(factories, report.getFactory().getId(), report.getFactory().getName()).add(report);
        }

        OverdueSummary overdue = summarizeOverdue(overdueRecords);
        long overdueMaintenanceCount = countOverdueMaintenance(asOfDate, factoryId);
        List<ExecutiveDashboardResponse.FactoryPerformance> factoryPerformance = factories.values().stream()
                .map(FactoryAggregate::toResponse)
                .sorted(Comparator.comparing(ExecutiveDashboardResponse.FactoryPerformance::getFactoryName,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();

        ExecutiveDashboardResponse.FinancialOverview financial = total.toFinancialOverview(overdue);
        ExecutiveDashboardResponse.ProductionOverview production = total.toProductionOverview();
        List<ExecutiveDashboardResponse.RiskItem> risks = buildRisks(
                financial, production, factoryPerformance, overdueMaintenanceCount);
        List<ExecutiveDashboardResponse.DecisionItem> decisions = risks.stream()
                .map(this::toDecision)
                .toList();

        return ExecutiveDashboardResponse.builder()
                .fromDate(range.fromDate())
                .toDate(range.toDate())
                .asOfDate(asOfDate)
                .scopeType(factoryId == null ? "COMPANY" : "FACTORY")
                .factoryId(factoryId)
                .scopeName(factoryName)
                .trendGranularity(range.granularity())
                .kpis(ExecutiveDashboardResponse.KpiOverview.builder()
                        .financial(financial)
                        .production(production)
                        .build())
                .trends(periods.values().stream().map(PeriodAggregate::toResponse).toList())
                .factories(factoryPerformance)
                .risks(risks)
                .decisions(decisions)
                .generatedAt(LocalDateTime.now())
                .build();
    }

    private DateRange resolveRange(LocalDate requestedFromDate, LocalDate requestedToDate) {
        LocalDate today = LocalDate.now();
        LocalDate toDate = requestedToDate == null ? today : requestedToDate;
        LocalDate fromDate = requestedFromDate == null ? toDate.withDayOfMonth(1) : requestedFromDate;
        long numberOfDays = ChronoUnit.DAYS.between(fromDate, toDate) + 1;
        if (toDate.isBefore(fromDate) || numberOfDays > MAX_RANGE_DAYS) {
            throw new AppException(ErrorCode.INVALID_FINANCIAL_DATE_RANGE);
        }
        String granularity = numberOfDays <= DAILY_TREND_MAX_DAYS ? "DAY" : "MONTH";
        return new DateRange(fromDate, toDate, granularity);
    }

    private String validateAndGetFactoryName(Long factoryId) {
        if (factoryId == null) {
            return "Toàn công ty";
        }
        return factoryRepository.findByIdAndActiveTrue(factoryId)
                .map(factory -> factory.getName())
                .orElseThrow(() -> new AppException(ErrorCode.FACTORY_ID_NOT_FOUND));
    }

    private Specification<FinancialRecord> financialPeriodSpec(
            LocalDate fromDate,
            LocalDate toDate,
            Long factoryId
    ) {
        return (root, query, cb) -> {
            root.fetch("category", JoinType.INNER);
            root.fetch("factory", JoinType.INNER);
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), FinancialRecordStatus.POSTED));
            predicates.add(cb.isTrue(root.get("active")));
            predicates.add(cb.between(root.get("recordDate"), fromDate, toDate));
            if (factoryId != null) {
                predicates.add(cb.equal(root.get("factory").get("id"), factoryId));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private Specification<ProductionReport> productionPeriodSpec(
            LocalDate fromDate,
            LocalDate toDate,
            Long factoryId
    ) {
        return (root, query, cb) -> {
            root.fetch("factory", JoinType.INNER);
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.between(root.get("reportDate"), fromDate, toDate));
            if (factoryId != null) {
                predicates.add(cb.equal(root.get("factory").get("id"), factoryId));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private Specification<FinancialRecord> overdueFinancialSpec(LocalDate asOfDate, Long factoryId) {
        return (root, query, cb) -> {
            root.fetch("category", JoinType.INNER);
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), FinancialRecordStatus.POSTED));
            predicates.add(cb.isTrue(root.get("active")));
            predicates.add(cb.lessThan(root.get("recordDate"), asOfDate.plusDays(1)));
            predicates.add(cb.isNotNull(root.get("dueDate")));
            predicates.add(cb.lessThan(root.get("dueDate"), asOfDate));
            predicates.add(cb.notEqual(root.get("paymentStatus"), PaymentStatus.PAID));
            if (factoryId != null) {
                predicates.add(cb.equal(root.get("factory").get("id"), factoryId));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }

    private long countOverdueMaintenance(LocalDate asOfDate, Long factoryId) {
        Specification<MaintenanceSchedule> scheduleSpec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            predicates.add(cb.lessThan(root.get("nextDueDate"), asOfDate));
            predicates.add(cb.isTrue(root.get("machine").get("active")));
            predicates.add(cb.isTrue(root.get("machine").get("team").get("active")));
            predicates.add(cb.isTrue(root.get("machine").get("team").get("productionLine").get("active")));
            predicates.add(cb.isTrue(root.get("machine").get("team").get("productionLine")
                    .get("department").get("active")));
            predicates.add(cb.isTrue(root.get("machine").get("team").get("productionLine")
                    .get("department").get("factory").get("active")));
            if (factoryId != null) {
                predicates.add(cb.equal(
                        root.get("machine").get("team").get("productionLine")
                                .get("department").get("factory").get("id"),
                        factoryId));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };

        EnumSet<MaintenanceWorkOrderStatus> closedStatuses = EnumSet.of(
                MaintenanceWorkOrderStatus.COMPLETED,
                MaintenanceWorkOrderStatus.CANCELLED);
        LocalDateTime endOfAsOfDate = asOfDate.equals(LocalDate.now())
                ? LocalDateTime.now()
                : asOfDate.atTime(LocalTime.MAX);
        Specification<MaintenanceWorkOrder> workOrderSpec = (root, query, cb) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
            predicates.add(cb.lessThan(root.get("plannedEnd"), endOfAsOfDate));
            predicates.add(root.get("status").in(closedStatuses).not());
            predicates.add(cb.isTrue(root.get("machine").get("active")));
            predicates.add(cb.isTrue(root.get("machine").get("team").get("active")));
            predicates.add(cb.isTrue(root.get("machine").get("team").get("productionLine").get("active")));
            predicates.add(cb.isTrue(root.get("machine").get("team").get("productionLine")
                    .get("department").get("active")));
            predicates.add(cb.isTrue(root.get("machine").get("team").get("productionLine")
                    .get("department").get("factory").get("active")));
            if (factoryId != null) {
                predicates.add(cb.equal(
                        root.get("machine").get("team").get("productionLine")
                                .get("department").get("factory").get("id"),
                        factoryId));
            }
            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };

        return maintenanceScheduleRepository.count(scheduleSpec)
                + maintenanceWorkOrderRepository.count(workOrderSpec);
    }

    private OverdueSummary summarizeOverdue(List<FinancialRecord> records) {
        BigDecimal receivable = BigDecimal.ZERO;
        BigDecimal payable = BigDecimal.ZERO;
        long receivableCount = 0;
        long payableCount = 0;
        for (FinancialRecord record : records) {
            BigDecimal outstanding = safe(record.getAmount()).subtract(safe(record.getPaidAmount()));
            if (outstanding.signum() <= 0) {
                continue;
            }
            if (record.getCategory().getEntryType() == FinancialEntryType.REVENUE) {
                receivable = receivable.add(outstanding);
                receivableCount++;
            } else {
                payable = payable.add(outstanding);
                payableCount++;
            }
        }
        return new OverdueSummary(money(receivable), receivableCount, money(payable), payableCount);
    }

    private Map<String, PeriodAggregate> initializePeriods(DateRange range) {
        Map<String, PeriodAggregate> result = new LinkedHashMap<>();
        if ("DAY".equals(range.granularity())) {
            LocalDate cursor = range.fromDate();
            while (!cursor.isAfter(range.toDate())) {
                result.put(cursor.toString(), new PeriodAggregate(cursor.toString(), cursor, cursor));
                cursor = cursor.plusDays(1);
            }
            return result;
        }

        YearMonth cursor = YearMonth.from(range.fromDate());
        YearMonth last = YearMonth.from(range.toDate());
        while (!cursor.isAfter(last)) {
            LocalDate periodStart = cursor.atDay(1).isBefore(range.fromDate())
                    ? range.fromDate() : cursor.atDay(1);
            LocalDate periodEnd = cursor.atEndOfMonth().isAfter(range.toDate())
                    ? range.toDate() : cursor.atEndOfMonth();
            result.put(cursor.toString(), new PeriodAggregate(cursor.toString(), periodStart, periodEnd));
            cursor = cursor.plusMonths(1);
        }
        return result;
    }

    private PeriodAggregate periodFor(
            Map<String, PeriodAggregate> periods,
            LocalDate date,
            String granularity
    ) {
        String key = "DAY".equals(granularity) ? date.toString() : YearMonth.from(date).toString();
        return periods.get(key);
    }

    private FactoryAggregate factoryFor(
            Map<Long, FactoryAggregate> factories,
            Long factoryId,
            String factoryName
    ) {
        return factories.computeIfAbsent(factoryId, ignored -> new FactoryAggregate(factoryId, factoryName));
    }

    private List<ExecutiveDashboardResponse.RiskItem> buildRisks(
            ExecutiveDashboardResponse.FinancialOverview financial,
            ExecutiveDashboardResponse.ProductionOverview production,
            List<ExecutiveDashboardResponse.FactoryPerformance> factories,
            long overdueMaintenanceCount
    ) {
        List<ExecutiveDashboardResponse.RiskItem> result = new ArrayList<>();
        if (financial.getProfit().signum() < 0) {
            result.add(risk(
                    "OPERATING_LOSS",
                    "CRITICAL",
                    "FINANCE",
                    "Kết quả kinh doanh đang lỗ",
                    "Chi phí ghi sổ đang cao hơn doanh thu trong kỳ được chọn.",
                    financial.getProfit(),
                    "VND",
                    ZERO,
                    countFactoriesWithLoss(factories)));
        }
        if (financial.getOverdueReceivable().signum() > 0) {
            result.add(risk(
                    "OVERDUE_RECEIVABLE",
                    "HIGH",
                    "FINANCE",
                    "Có công nợ phải thu quá hạn",
                    "Dòng tiền có nguy cơ bị ảnh hưởng bởi các khoản phải thu chưa thanh toán đúng hạn.",
                    financial.getOverdueReceivable(),
                    "VND",
                    ZERO,
                    financial.getOverdueReceivableCount()));
        }
        if (financial.getOverduePayable().signum() > 0) {
            result.add(risk(
                    "OVERDUE_PAYABLE",
                    "HIGH",
                    "FINANCE",
                    "Có công nợ phải trả quá hạn",
                    "Các khoản phải trả quá hạn có thể ảnh hưởng quan hệ nhà cung cấp và tiến độ vật tư.",
                    financial.getOverduePayable(),
                    "VND",
                    ZERO,
                    financial.getOverduePayableCount()));
        }
        if (production.getPlannedQuantity() > 0
                && production.getPlanAttainmentPercent().compareTo(MIN_PLAN_ATTAINMENT) < 0) {
            result.add(risk(
                    "LOW_PLAN_ATTAINMENT",
                    production.getPlanAttainmentPercent().compareTo(new BigDecimal("75.00")) < 0
                            ? "HIGH" : "MEDIUM",
                    "PRODUCTION",
                    "Sản lượng chưa đạt kế hoạch",
                    "Tỷ lệ hoàn thành sản lượng thấp hơn ngưỡng điều hành 90%.",
                    production.getPlanAttainmentPercent(),
                    "%",
                    MIN_PLAN_ATTAINMENT,
                    countFactoriesBelowAttainment(factories)));
        }
        if (production.getActualQuantity() > 0
                && production.getDefectRatePercent().compareTo(MAX_DEFECT_RATE) > 0) {
            result.add(risk(
                    "HIGH_DEFECT_RATE",
                    production.getDefectRatePercent().compareTo(new BigDecimal("5.00")) > 0
                            ? "HIGH" : "MEDIUM",
                    "QUALITY",
                    "Tỷ lệ lỗi vượt ngưỡng",
                    "Tỷ lệ sản phẩm lỗi cao hơn ngưỡng điều hành 3%.",
                    production.getDefectRatePercent(),
                    "%",
                    MAX_DEFECT_RATE,
                    countFactoriesAboveDefectRate(factories)));
        }
        if (production.getOfficialReportCount() > 0
                && production.getOeePercent().compareTo(MIN_OEE) < 0) {
            result.add(risk(
                    "LOW_OEE",
                    production.getOeePercent().compareTo(new BigDecimal("60.00")) < 0
                            ? "HIGH" : "MEDIUM",
                    "EFFICIENCY",
                    "Hiệu suất OEE dưới mục tiêu",
                    "OEE tổng hợp thấp hơn ngưỡng điều hành 75%; dashboard không công khai máy cụ thể.",
                    production.getOeePercent(),
                    "%",
                    MIN_OEE,
                    countFactoriesBelowOee(factories)));
        }
        if (overdueMaintenanceCount > 0) {
            result.add(risk(
                    "OVERDUE_MAINTENANCE",
                    overdueMaintenanceCount >= 5 ? "HIGH" : "MEDIUM",
                    "MAINTENANCE",
                    "Có công việc bảo trì quá hạn",
                    "Có lịch hoặc phiếu bảo trì đã quá hạn; chỉ hiển thị tổng số, không hiển thị máy cụ thể.",
                    BigDecimal.valueOf(overdueMaintenanceCount),
                    "TASK",
                    ZERO,
                    overdueMaintenanceCount));
        }
        return result;
    }

    private ExecutiveDashboardResponse.RiskItem risk(
            String code,
            String severity,
            String domain,
            String title,
            String description,
            BigDecimal metricValue,
            String unit,
            BigDecimal threshold,
            long affectedCount
    ) {
        return ExecutiveDashboardResponse.RiskItem.builder()
                .code(code)
                .severity(severity)
                .domain(domain)
                .title(title)
                .description(description)
                .metricValue(metricValue)
                .unit(unit)
                .threshold(threshold)
                .affectedCount(affectedCount)
                .build();
    }

    private ExecutiveDashboardResponse.DecisionItem toDecision(ExecutiveDashboardResponse.RiskItem risk) {
        String action = switch (risk.getCode()) {
            case "OPERATING_LOSS" -> "Yêu cầu tài chính phân tích biến động chi phí và lập phương án khôi phục biên lợi nhuận.";
            case "OVERDUE_RECEIVABLE" -> "Phân công xử lý thu hồi công nợ theo mức quá hạn và mức ảnh hưởng dòng tiền.";
            case "OVERDUE_PAYABLE" -> "Chốt kế hoạch thanh toán và ưu tiên nhà cung cấp ảnh hưởng trực tiếp đến sản xuất.";
            case "LOW_PLAN_ATTAINMENT" -> "Rà soát năng lực, vật tư và kế hoạch ca; quyết định điều chỉnh kế hoạch hoặc nguồn lực.";
            case "HIGH_DEFECT_RATE" -> "Yêu cầu bộ phận chất lượng mở phân tích nguyên nhân và kế hoạch hành động khắc phục.";
            case "LOW_OEE" -> "Yêu cầu quản lý sản xuất lập kế hoạch cải thiện Availability, Performance và Quality.";
            case "OVERDUE_MAINTENANCE" -> "Ưu tiên nguồn lực bảo trì và chốt thời hạn xử lý theo mức ảnh hưởng vận hành.";
            default -> "Phân công đơn vị phụ trách phân tích và đề xuất hành động xử lý.";
        };
        return ExecutiveDashboardResponse.DecisionItem.builder()
                .priority(risk.getSeverity())
                .domain(risk.getDomain())
                .title("Quyết định: " + risk.getTitle())
                .rationale(risk.getDescription())
                .recommendedAction(action)
                .relatedRiskCode(risk.getCode())
                .build();
    }

    private long countFactoriesWithLoss(List<ExecutiveDashboardResponse.FactoryPerformance> factories) {
        return ensureAtLeastOne(factories.stream().filter(value -> value.getProfit().signum() < 0).count());
    }

    private long countFactoriesBelowAttainment(List<ExecutiveDashboardResponse.FactoryPerformance> factories) {
        return ensureAtLeastOne(factories.stream()
                .filter(value -> value.getPlannedQuantity() > 0)
                .filter(value -> value.getPlanAttainmentPercent().compareTo(MIN_PLAN_ATTAINMENT) < 0)
                .count());
    }

    private long countFactoriesAboveDefectRate(List<ExecutiveDashboardResponse.FactoryPerformance> factories) {
        return ensureAtLeastOne(factories.stream()
                .filter(value -> value.getActualQuantity() > 0)
                .filter(value -> percent(value.getDefectQuantity(), value.getActualQuantity())
                        .compareTo(MAX_DEFECT_RATE) > 0)
                .count());
    }

    private long countFactoriesBelowOee(List<ExecutiveDashboardResponse.FactoryPerformance> factories) {
        return ensureAtLeastOne(factories.stream()
                .filter(value -> value.getOfficialReportCount() > 0)
                .filter(value -> value.getOeePercent().compareTo(MIN_OEE) < 0)
                .count());
    }

    private long ensureAtLeastOne(long value) {
        return value == 0 ? 1 : value;
    }

    private static BigDecimal safe(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }

    private static BigDecimal money(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal decimal(BigDecimal value) {
        return safe(value).setScale(2, RoundingMode.HALF_UP);
    }

    private static BigDecimal percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return ZERO;
        }
        return BigDecimal.valueOf(numerator)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal perHour(long quantity, long workingMinutes) {
        if (workingMinutes <= 0) {
            return ZERO;
        }
        return BigDecimal.valueOf(quantity)
                .multiply(BigDecimal.valueOf(60))
                .divide(BigDecimal.valueOf(workingMinutes), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal weightedAverage(BigDecimal weightedTotal, long weight) {
        if (weight <= 0) {
            return ZERO;
        }
        return weightedTotal.divide(BigDecimal.valueOf(weight), 2, RoundingMode.HALF_UP);
    }

    private record DateRange(LocalDate fromDate, LocalDate toDate, String granularity) {
    }

    private record OverdueSummary(
            BigDecimal receivable,
            long receivableCount,
            BigDecimal payable,
            long payableCount
    ) {
    }

    private static class Aggregate {
        protected BigDecimal revenue = BigDecimal.ZERO;
        protected BigDecimal expense = BigDecimal.ZERO;
        protected long postedRecordCount;
        protected long officialReportCount;
        protected long plannedQuantity;
        protected long actualQuantity;
        protected long goodQuantity;
        protected long defectQuantity;
        protected long workingMinutes;
        protected long downtimeMinutes;
        protected BigDecimal weightedAvailability = BigDecimal.ZERO;
        protected BigDecimal weightedPerformance = BigDecimal.ZERO;
        protected BigDecimal weightedOee = BigDecimal.ZERO;
        protected long metricWeight;

        void add(FinancialRecord record) {
            BigDecimal amount = safe(record.getAmount());
            if (record.getCategory().getEntryType() == FinancialEntryType.REVENUE) {
                revenue = revenue.add(amount);
            } else {
                expense = expense.add(amount);
            }
            postedRecordCount++;
        }

        void add(ProductionReport report) {
            long weight = Math.max(report.getWorkingMinutes() == null ? 0 : report.getWorkingMinutes(), 1);
            officialReportCount++;
            plannedQuantity += nullSafe(report.getPlannedQuantity());
            actualQuantity += nullSafe(report.getActualQuantity());
            goodQuantity += nullSafe(report.getGoodQuantity());
            defectQuantity += nullSafe(report.getDefectQuantity());
            workingMinutes += nullSafe(report.getWorkingMinutes());
            downtimeMinutes += nullSafe(report.getDowntimeMinutes());
            weightedAvailability = weightedAvailability.add(safe(report.getAvailability())
                    .multiply(BigDecimal.valueOf(weight)));
            weightedPerformance = weightedPerformance.add(safe(report.getPerformance())
                    .multiply(BigDecimal.valueOf(weight)));
            weightedOee = weightedOee.add(safe(report.getOee()).multiply(BigDecimal.valueOf(weight)));
            metricWeight += weight;
        }

        ExecutiveDashboardResponse.FinancialOverview toFinancialOverview(OverdueSummary overdue) {
            BigDecimal profit = revenue.subtract(expense);
            BigDecimal margin = revenue.signum() == 0
                    ? ZERO
                    : profit.multiply(BigDecimal.valueOf(100))
                            .divide(revenue, 2, RoundingMode.HALF_UP);
            return ExecutiveDashboardResponse.FinancialOverview.builder()
                    .revenue(money(revenue))
                    .expense(money(expense))
                    .profit(money(profit))
                    .profitMarginPercent(decimal(margin))
                    .overdueReceivable(overdue.receivable())
                    .overdueReceivableCount(overdue.receivableCount())
                    .overduePayable(overdue.payable())
                    .overduePayableCount(overdue.payableCount())
                    .postedRecordCount(postedRecordCount)
                    .build();
        }

        ExecutiveDashboardResponse.ProductionOverview toProductionOverview() {
            return ExecutiveDashboardResponse.ProductionOverview.builder()
                    .officialReportCount(officialReportCount)
                    .plannedQuantity(plannedQuantity)
                    .actualQuantity(actualQuantity)
                    .goodQuantity(goodQuantity)
                    .defectQuantity(defectQuantity)
                    .workingMinutes(workingMinutes)
                    .downtimeMinutes(downtimeMinutes)
                    .planAttainmentPercent(percent(actualQuantity, plannedQuantity))
                    .productivityPerHour(perHour(goodQuantity, operatingMinutes()))
                    .defectRatePercent(percent(defectQuantity, actualQuantity))
                    .qualityPercent(percent(goodQuantity, actualQuantity))
                    .availabilityPercent(weightedAverage(weightedAvailability, metricWeight))
                    .performancePercent(weightedAverage(weightedPerformance, metricWeight))
                    .oeePercent(weightedAverage(weightedOee, metricWeight))
                    .build();
        }

        protected long operatingMinutes() {
            return Math.max(workingMinutes - downtimeMinutes, 0L);
        }

        private static long nullSafe(Long value) {
            return value == null ? 0L : value;
        }

        private static int nullSafe(Integer value) {
            return value == null ? 0 : value;
        }
    }

    private static class PeriodAggregate extends Aggregate {
        private final String period;
        private final LocalDate periodStart;
        private final LocalDate periodEnd;

        private PeriodAggregate(String period, LocalDate periodStart, LocalDate periodEnd) {
            this.period = period;
            this.periodStart = periodStart;
            this.periodEnd = periodEnd;
        }

        ExecutiveDashboardResponse.TrendPoint toResponse() {
            BigDecimal profit = revenue.subtract(expense);
            return ExecutiveDashboardResponse.TrendPoint.builder()
                    .period(period)
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .revenue(money(revenue))
                    .expense(money(expense))
                    .profit(money(profit))
                    .plannedQuantity(plannedQuantity)
                    .actualQuantity(actualQuantity)
                    .goodQuantity(goodQuantity)
                    .defectQuantity(defectQuantity)
                    .planAttainmentPercent(percent(actualQuantity, plannedQuantity))
                    .productivityPerHour(perHour(goodQuantity, operatingMinutes()))
                    .qualityPercent(percent(goodQuantity, actualQuantity))
                    .oeePercent(weightedAverage(weightedOee, metricWeight))
                    .build();
        }
    }

    private static class FactoryAggregate extends Aggregate {
        private final Long factoryId;
        private final String factoryName;

        private FactoryAggregate(Long factoryId, String factoryName) {
            this.factoryId = factoryId;
            this.factoryName = factoryName;
        }

        ExecutiveDashboardResponse.FactoryPerformance toResponse() {
            return ExecutiveDashboardResponse.FactoryPerformance.builder()
                    .factoryId(factoryId)
                    .factoryName(factoryName)
                    .officialReportCount(officialReportCount)
                    .revenue(money(revenue))
                    .expense(money(expense))
                    .profit(money(revenue.subtract(expense)))
                    .plannedQuantity(plannedQuantity)
                    .actualQuantity(actualQuantity)
                    .goodQuantity(goodQuantity)
                    .defectQuantity(defectQuantity)
                    .planAttainmentPercent(percent(actualQuantity, plannedQuantity))
                    .productivityPerHour(perHour(goodQuantity, operatingMinutes()))
                    .qualityPercent(percent(goodQuantity, actualQuantity))
                    .oeePercent(weightedAverage(weightedOee, metricWeight))
                    .build();
        }
    }
}
