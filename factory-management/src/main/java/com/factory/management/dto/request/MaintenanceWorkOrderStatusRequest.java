package com.factory.management.dto.request;

import com.factory.management.entity.MaintenanceWorkOrderStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

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
