package com.factory.management.modules.maintenance.dto.request;

import com.factory.management.modules.maintenance.entity.MaintenancePriority;
import com.factory.management.modules.maintenance.entity.MaintenanceType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaintenanceWorkOrderRequest {
    private Long maintenanceRequestId;
    private Long maintenanceScheduleId;

    @NotNull(message = "NOT_NULL_MAINTENANCE_MACHINE_ID")
    private Long machineId;

    @NotNull(message = "NOT_NULL_MAINTENANCE_TYPE")
    private MaintenanceType maintenanceType;

    @NotNull(message = "NOT_NULL_MAINTENANCE_PRIORITY")
    private MaintenancePriority priority;

    private Long assignedEmployeeId;

    @NotBlank(message = "NOT_BLANK_MAINTENANCE_TITLE")
    @Size(max = 255, message = "SIZE_MAINTENANCE_TITLE")
    private String title;

    @Size(max = 2000, message = "SIZE_MAINTENANCE_DESCRIPTION")
    private String description;

    @NotNull(message = "NOT_NULL_MAINTENANCE_PLANNED_START")
    private LocalDateTime plannedStart;

    @NotNull(message = "NOT_NULL_MAINTENANCE_PLANNED_END")
    private LocalDateTime plannedEnd;

    @DecimalMin(value = "0", message = "INVALID_MAINTENANCE_COST")
    private BigDecimal laborCost;

    @DecimalMin(value = "0", message = "INVALID_MAINTENANCE_COST")
    private BigDecimal externalCost;
}
