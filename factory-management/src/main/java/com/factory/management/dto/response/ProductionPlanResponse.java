package com.factory.management.dto.response;

import com.factory.management.entity.ProductionPlanStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductionPlanResponse {
    private Long id;
    private String planNo;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Long factoryId;
    private String factoryCode;
    private String factoryName;
    private Long productionLineId;
    private String productionLineCode;
    private String productionLineName;
    private Long productId;
    private String productCode;
    private String productName;
    private String productUnit;
    private BigDecimal plannedQuantity;
    private BigDecimal allocatedQuantity;
    private ProductionPlanStatus status;
    private String note;
    private Boolean active;
    private LocalDateTime approvedAt;
    private String approvedBy;
    private LocalDateTime closedAt;
    private String closedBy;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
