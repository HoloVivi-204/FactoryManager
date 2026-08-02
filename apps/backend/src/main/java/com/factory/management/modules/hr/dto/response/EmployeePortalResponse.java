package com.factory.management.modules.hr.dto.response;

import com.factory.management.modules.hr.entity.AttendanceStatus;
import com.factory.management.modules.hr.entity.LeaveStatus;
import com.factory.management.modules.hr.entity.NotificationSeverity;
import com.factory.management.modules.hr.entity.OvertimeStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EmployeePortalResponse {
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String position;
    private String teamName;
    private Integer workingMinutes;
    private Integer overtimeMinutes;
    private Long unreadNotifications;
    private List<ScheduleItem> schedules;
    private List<AttendanceItem> attendance;
    private List<KpiItem> kpis;
    private List<LeaveItem> leaves;
    private List<OvertimeItem> overtimeRequests;
    private List<NotificationItem> notifications;

    @Getter
    @Builder
    public static class ScheduleItem {
        private Long id;
        private LocalDate workDate;
        private String shiftCode;
        private String shiftName;
        private LocalTime startTime;
        private LocalTime endTime;
        private String note;
    }

    @Getter
    @Builder
    public static class AttendanceItem {
        private Long id;
        private LocalDate workDate;
        private LocalDateTime checkIn;
        private LocalDateTime checkOut;
        private Integer workingMinutes;
        private Integer overtimeMinutes;
        private AttendanceStatus status;
    }

    @Getter
    @Builder
    public static class KpiItem {
        private Long id;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private BigDecimal score;
        private BigDecimal productivityScore;
        private BigDecimal qualityScore;
        private BigDecimal attendanceScore;
        private String note;
    }

    @Getter
    @Builder
    public static class LeaveItem {
        private Long id;
        private LocalDate fromDate;
        private LocalDate toDate;
        private String leaveType;
        private String reason;
        private LeaveStatus status;
        private String reviewComment;
        private LocalDateTime reviewedAt;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class OvertimeItem {
        private Long id;
        private LocalDate workDate;
        private Integer requestedMinutes;
        private String reason;
        private OvertimeStatus status;
        private String reviewComment;
        private LocalDateTime reviewedAt;
        private LocalDateTime createdAt;
    }

    @Getter
    @Builder
    public static class NotificationItem {
        private Long id;
        private String title;
        private String message;
        private NotificationSeverity severity;
        private String actionUrl;
        private Boolean read;
        private LocalDateTime readAt;
        private LocalDateTime createdAt;
    }
}
