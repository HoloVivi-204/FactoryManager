package com.factory.management.modules.production.dto.response;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
