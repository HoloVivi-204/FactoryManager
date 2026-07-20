package com.factory.management.dto.response;
import com.factory.management.entity.*;import lombok.*;import java.math.BigDecimal;import java.time.LocalDateTime;import java.util.List;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor public class ProductionReportDetailResponse {
 ProductionReportResponse report; List<DowntimeItem> downtimes; List<QualityItem> qualityErrors; List<MaterialItem> materialIssues; List<EmployeeItem> employees;
 @Getter @Builder public static class DowntimeItem {Long id;Long machineId;String machineCode;Long downtimeReasonId;String downtimeReasonName;LocalDateTime startTime;LocalDateTime endTime;Integer durationMinutes;String description;}
 @Getter @Builder public static class QualityItem {Long id;Long qualityErrorTypeId;String qualityErrorTypeName;Long quantity;String description;}
 @Getter @Builder public static class MaterialItem {Long id;Long materialId;String materialName;MaterialIssueType issueType;BigDecimal quantity;String unit;String description;}
 @Getter @Builder public static class EmployeeItem {Long id;Long employeeId;String employeeCode;String employeeName;Integer workingMinutes;Integer overtimeMinutes;AttendanceStatus attendanceStatus;AssignmentType assignmentType;String description;}
}
