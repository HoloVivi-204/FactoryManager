package com.factory.management.modules.maintenance.dto.response;

import com.factory.management.modules.maintenance.entity.MachineOperationalStatus;
import com.factory.management.modules.maintenance.entity.MaintenancePriority;
import com.factory.management.modules.maintenance.entity.MaintenanceRequestStatus;
import com.factory.management.modules.maintenance.entity.MaintenanceType;
import com.factory.management.modules.maintenance.entity.MaintenanceWorkOrderStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

public final class MaintenanceResponse {
    private MaintenanceResponse() {
    }

    @Getter
    @Builder
    public static class MachineOption {
        private Long id;
        private String code;
        private String name;
        private Long teamId;
        private String teamName;
        private MachineOperationalStatus operationalStatus;
    }

    @Getter
    @Builder
    public static class RequestItem {
        private Long id;
        private String requestNo;
        private Long machineId;
        private String machineCode;
        private String machineName;
        private Long teamId;
        private String teamName;
        private Long reportedById;
        private String reportedByName;
        private Long sourceDowntimeStagingId;
        private MaintenancePriority priority;
        private MaintenanceRequestStatus status;
        private String title;
        private String description;
        private String impactDescription;
        private LocalDateTime reportedAt;
        private LocalDateTime resolvedAt;
        private String resolutionNote;
        private Long version;
    }

    @Getter
    @Builder
    public static class ScheduleItem {
        private Long id;
        private Long machineId;
        private String machineCode;
        private String machineName;
        private Long teamId;
        private String teamName;
        private MaintenanceType maintenanceType;
        private String name;
        private Integer intervalDays;
        private LocalDate lastCompletedDate;
        private LocalDate nextDueDate;
        private String description;
        private Boolean active;
        private Long version;
    }

    @Getter
    @Builder
    public static class WorkOrderItem {
        private Long id;
        private String workOrderNo;
        private Long maintenanceRequestId;
        private Long maintenanceScheduleId;
        private Long machineId;
        private String machineCode;
        private String machineName;
        private Long teamId;
        private String teamName;
        private MaintenanceType maintenanceType;
        private MaintenancePriority priority;
        private MaintenanceWorkOrderStatus status;
        private Long assignedEmployeeId;
        private String assignedEmployeeName;
        private String title;
        private String description;
        private LocalDateTime plannedStart;
        private LocalDateTime plannedEnd;
        private LocalDateTime actualStart;
        private LocalDateTime actualEnd;
        private BigDecimal laborCost;
        private BigDecimal partCost;
        private BigDecimal externalCost;
        private BigDecimal totalCost;
        private String completionNote;
        private List<PartUsageItem> parts;
        private Long version;
    }

    @Getter
    @Builder
    public static class PartUsageItem {
        private Long id;
        private Long materialId;
        private String materialCode;
        private String materialName;
        private BigDecimal quantity;
        private String unit;
        private BigDecimal unitCost;
        private BigDecimal totalCost;
    }

    @Getter
    @Builder
    public static class StatusHistoryItem {
        private Long id;
        private Long machineId;
        private MachineOperationalStatus previousStatus;
        private MachineOperationalStatus newStatus;
        private String sourceType;
        private Long sourceId;
        private String note;
        private String changedBy;
        private LocalDateTime changedAt;
    }

    @Getter
    @Builder
    public static class Dashboard {
        private long openRequests;
        private long criticalRequests;
        private long overdueSchedules;
        private long activeWorkOrders;
        private BigDecimal completedCost;
    }
}
