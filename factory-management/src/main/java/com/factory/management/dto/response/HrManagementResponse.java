package com.factory.management.dto.response;

import com.factory.management.entity.AssignmentType;
import com.factory.management.entity.AttendanceSource;
import com.factory.management.entity.AttendanceStatus;
import com.factory.management.entity.LeaveStatus;
import com.factory.management.entity.NotificationSeverity;
import com.factory.management.entity.OvertimeStatus;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

public final class HrManagementResponse {
    private HrManagementResponse() {
    }

    @Getter
    @Builder
    public static class ScheduleItem {
        private Long id;
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private Long teamId;
        private String teamName;
        private Long shiftId;
        private String shiftCode;
        private String shiftName;
        private LocalDate workDate;
        private String note;
        private Boolean active;
        private Long version;
    }

    @Getter
    @Builder
    public static class AttendanceItem {
        private Long id;
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private Long teamId;
        private String teamName;
        private LocalDate workDate;
        private LocalDateTime checkIn;
        private LocalDateTime checkOut;
        private Integer workingMinutes;
        private Integer overtimeMinutes;
        private AttendanceStatus attendanceStatus;
        private AttendanceSource source;
        private String note;
        private String confirmedByName;
        private Long version;
    }

    @Getter
    @Builder
    public static class LeaveItem {
        private Long id;
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private Long teamId;
        private String teamName;
        private LocalDate fromDate;
        private LocalDate toDate;
        private String leaveType;
        private String reason;
        private LeaveStatus status;
        private String approvedByName;
        private String reviewComment;
        private LocalDateTime reviewedAt;
        private LocalDateTime createdAt;
        private Long version;
    }

    @Getter
    @Builder
    public static class KpiItem {
        private Long id;
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private Long teamId;
        private String teamName;
        private LocalDate periodStart;
        private LocalDate periodEnd;
        private BigDecimal score;
        private BigDecimal productivityScore;
        private BigDecimal qualityScore;
        private BigDecimal attendanceScore;
        private String note;
        private Long version;
    }

    @Getter
    @Builder
    public static class OvertimeItem {
        private Long id;
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private Long teamId;
        private String teamName;
        private LocalDate workDate;
        private Integer requestedMinutes;
        private String reason;
        private OvertimeStatus status;
        private String approvedByName;
        private String reviewComment;
        private LocalDateTime reviewedAt;
        private LocalDateTime createdAt;
        private Long version;
    }

    @Getter
    @Builder
    public static class AssignmentItem {
        private Long id;
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private Long sourceTeamId;
        private String sourceTeamName;
        private Long targetTeamId;
        private String targetTeamName;
        private AssignmentType assignmentType;
        private LocalDate effectiveFrom;
        private LocalDate effectiveTo;
        private String reason;
        private String approvedByName;
        private Boolean active;
        private Long version;
    }

    @Getter
    @Builder
    public static class NotificationItem {
        private Long id;
        private Long recipientEmployeeId;
        private String recipientName;
        private String title;
        private String message;
        private NotificationSeverity severity;
        private String actionUrl;
        private Boolean read;
        private LocalDateTime readAt;
        private LocalDateTime createdAt;
    }
}
