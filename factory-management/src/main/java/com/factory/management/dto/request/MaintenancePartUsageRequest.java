package com.factory.management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaintenancePartUsageRequest {
    @NotNull(message = "NOT_NULL_MAINTENANCE_MATERIAL_ID")
    private Long materialId;

    @NotNull(message = "NOT_NULL_MAINTENANCE_PART_QUANTITY")
    @DecimalMin(value = "0.001", message = "INVALID_MAINTENANCE_PART_QUANTITY")
    private BigDecimal quantity;

    @NotNull(message = "NOT_NULL_MAINTENANCE_PART_UNIT_COST")
    @DecimalMin(value = "0", message = "INVALID_MAINTENANCE_COST")
    private BigDecimal unitCost;
}
