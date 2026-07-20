package com.factory.management.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;
import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductionDashboardResponse {
    Long reportCount;
    Long plannedQuantity;
    Long actualQuantity;
    Long goodQuantity;
    Long defectQuantity;
    Long downtimeMinutes;
    BigDecimal averageAvailability;
    BigDecimal averagePerformance;
    BigDecimal averageQuality;
    BigDecimal averageOee;
    String scopeType;
    Long scopeId;
    String scopeName;
}
