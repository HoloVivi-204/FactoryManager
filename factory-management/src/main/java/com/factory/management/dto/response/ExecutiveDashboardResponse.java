package com.factory.management.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Dữ liệu tổng hợp dành cho màn hình điều hành. DTO này cố ý chỉ chứa chỉ số
 * tổng hợp theo thời gian/nhà máy, không trả về danh tính máy hoặc nhân viên.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExecutiveDashboardResponse {
    private LocalDate fromDate;
    private LocalDate toDate;
    private LocalDate asOfDate;
    private String scopeType;
    private Long factoryId;
    private String scopeName;
    private String trendGranularity;
    private KpiOverview kpis;
    private List<TrendPoint> trends;
    private List<FactoryPerformance> factories;
    private List<RiskItem> risks;
    private List<DecisionItem> decisions;
    private LocalDateTime generatedAt;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class KpiOverview {
        private FinancialOverview financial;
        private ProductionOverview production;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FinancialOverview {
        private BigDecimal revenue;
        private BigDecimal expense;
        private BigDecimal profit;
        private BigDecimal profitMarginPercent;
        private BigDecimal overdueReceivable;
        private long overdueReceivableCount;
        private BigDecimal overduePayable;
        private long overduePayableCount;
        private long postedRecordCount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ProductionOverview {
        private long officialReportCount;
        private long plannedQuantity;
        private long actualQuantity;
        private long goodQuantity;
        private long defectQuantity;
        private long workingMinutes;
        private long downtimeMinutes;
        private BigDecimal planAttainmentPercent;
        private BigDecimal productivityPerHour;
        private BigDecimal defectRatePercent;
        private BigDecimal qualityPercent;
        private BigDecimal availabilityPercent;
        private BigDecimal performancePercent;
        private BigDecimal oeePercent;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendPoint {
        private String period;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private BigDecimal revenue;
        private BigDecimal expense;
        private BigDecimal profit;
        private long plannedQuantity;
        private long actualQuantity;
        private long goodQuantity;
        private long defectQuantity;
        private BigDecimal planAttainmentPercent;
        private BigDecimal productivityPerHour;
        private BigDecimal qualityPercent;
        private BigDecimal oeePercent;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FactoryPerformance {
        private Long factoryId;
        private String factoryName;
        private long officialReportCount;
        private BigDecimal revenue;
        private BigDecimal expense;
        private BigDecimal profit;
        private long plannedQuantity;
        private long actualQuantity;
        private long goodQuantity;
        private long defectQuantity;
        private BigDecimal planAttainmentPercent;
        private BigDecimal productivityPerHour;
        private BigDecimal qualityPercent;
        private BigDecimal oeePercent;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RiskItem {
        private String code;
        private String severity;
        private String domain;
        private String title;
        private String description;
        private BigDecimal metricValue;
        private String unit;
        private BigDecimal threshold;
        private long affectedCount;
    }

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DecisionItem {
        private String priority;
        private String domain;
        private String title;
        private String rationale;
        private String recommendedAction;
        private String relatedRiskCode;
    }
}
