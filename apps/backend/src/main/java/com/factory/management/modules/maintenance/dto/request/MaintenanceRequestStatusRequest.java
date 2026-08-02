package com.factory.management.modules.maintenance.dto.request;

import com.factory.management.modules.maintenance.entity.MaintenanceRequestStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaintenanceRequestStatusRequest {
    @NotNull(message = "NOT_NULL_MAINTENANCE_REQUEST_STATUS")
    private MaintenanceRequestStatus status;

    @Size(max = 2000, message = "SIZE_MAINTENANCE_RESOLUTION_NOTE")
    private String resolutionNote;
}
