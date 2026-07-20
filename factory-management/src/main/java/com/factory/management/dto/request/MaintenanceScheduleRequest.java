package com.factory.management.dto.request;

import com.factory.management.entity.MaintenanceType;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class MaintenanceScheduleRequest {
    @NotNull(message = "NOT_NULL_MAINTENANCE_MACHINE_ID")
    private Long machineId;

    @NotNull(message = "NOT_NULL_MAINTENANCE_TYPE")
    private MaintenanceType maintenanceType;

    @NotBlank(message = "NOT_BLANK_MAINTENANCE_SCHEDULE_NAME")
    @Size(max = 255, message = "SIZE_MAINTENANCE_SCHEDULE_NAME")
    private String name;

    @NotNull(message = "NOT_NULL_MAINTENANCE_INTERVAL")
    @Min(value = 1, message = "INVALID_MAINTENANCE_INTERVAL")
    @Max(value = 3650, message = "INVALID_MAINTENANCE_INTERVAL")
    private Integer intervalDays;

    private LocalDate lastCompletedDate;

    @NotNull(message = "NOT_NULL_MAINTENANCE_NEXT_DUE_DATE")
    private LocalDate nextDueDate;

    @Size(max = 2000, message = "SIZE_MAINTENANCE_DESCRIPTION")
    private String description;

    private Boolean active;
}
