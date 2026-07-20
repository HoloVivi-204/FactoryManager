package com.factory.management.dto.request;

import com.factory.management.entity.MaintenancePriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaintenanceRequestCreateRequest {
    @NotNull(message = "NOT_NULL_MAINTENANCE_MACHINE_ID")
    private Long machineId;

    private Long sourceDowntimeStagingId;

    @NotNull(message = "NOT_NULL_MAINTENANCE_PRIORITY")
    private MaintenancePriority priority;

    @NotBlank(message = "NOT_BLANK_MAINTENANCE_TITLE")
    @Size(max = 255, message = "SIZE_MAINTENANCE_TITLE")
    private String title;

    @NotBlank(message = "NOT_BLANK_MAINTENANCE_DESCRIPTION")
    @Size(max = 2000, message = "SIZE_MAINTENANCE_DESCRIPTION")
    private String description;

    @Size(max = 1000, message = "SIZE_MAINTENANCE_IMPACT")
    private String impactDescription;
}
