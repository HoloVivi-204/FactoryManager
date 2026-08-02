package com.factory.management.dto.response;

import com.factory.management.entity.AssignmentType;
import com.factory.management.entity.AttendanceStatus;
import com.factory.management.entity.MaterialIssueType;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductionReportDetailResponse {
    private ProductionReportResponse report;
    private List<DowntimeItem> downtimes;
    private List<QualityItem> qualityErrors;
    private List<MaterialItem> materialIssues;
    private List<EmployeeItem> employees;

    @Getter
    @Builder
    public static class DowntimeItem {
        private Long id;
        private Long machineId;
        private String machineCode;
        private Long downtimeReasonId;
        private String downtimeReasonName;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Integer durationMinutes;
        private String description;
    }

    @Getter
    @Builder
    public static class QualityItem {
        private Long id;
        private Long qualityErrorTypeId;
        private String qualityErrorTypeName;
        private Long quantity;
        private String description;
    }

    @Getter
    @Builder
    public static class MaterialItem {
        private Long id;
        private Long materialId;
        private String materialName;
        private MaterialIssueType issueType;
        private BigDecimal quantity;
        private String unit;
        private String description;
    }

    @Getter
    @Builder
    public static class EmployeeItem {
        private Long id;
        private Long employeeId;
        private String employeeCode;
        private String employeeName;
        private Integer workingMinutes;
        private Integer overtimeMinutes;
        private AttendanceStatus attendanceStatus;
        private AssignmentType assignmentType;
        private String description;
    }
}
