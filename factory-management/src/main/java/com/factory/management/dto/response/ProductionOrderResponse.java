package com.factory.management.dto.response;

import com.factory.management.entity.ProductionOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProductionOrderResponse {
    private Long id;
    private String orderNo;
    private Long productionPlanId;
    private String productionPlanNo;
    private Long productId;
    private String productCode;
    private String productName;
    private Long teamId;
    private String teamCode;
    private String teamName;
    private Long machineId;
    private String machineCode;
    private String machineName;
    private Long shiftId;
    private String shiftCode;
    private String shiftName;
    private LocalDate scheduledStart;
    private LocalDate scheduledEnd;
    private BigDecimal plannedQuantity;
    private ProductionOrderStatus status;
    private String note;
    private Boolean active;
    private LocalDateTime releasedAt;
    private LocalDateTime startedAt;
    private LocalDateTime completedAt;
    private String createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;
}
