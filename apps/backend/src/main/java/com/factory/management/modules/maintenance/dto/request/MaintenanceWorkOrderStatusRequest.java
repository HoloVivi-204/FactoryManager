package com.factory.management.modules.maintenance.dto.request;

import com.factory.management.modules.maintenance.entity.MaintenanceWorkOrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MaintenanceWorkOrderStatusRequest {
    @NotNull(message = "NOT_NULL_MAINTENANCE_WORK_ORDER_STATUS")
    private MaintenanceWorkOrderStatus status;
    private LocalDateTime actualStart;
    private LocalDateTime actualEnd;

    @Size(max = 2000, message = "SIZE_MAINTENANCE_COMPLETION_NOTE")
    private String completionNote;
}
