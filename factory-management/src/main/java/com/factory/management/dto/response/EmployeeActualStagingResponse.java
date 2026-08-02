package com.factory.management.dto.response;

import com.factory.management.entity.AssignmentType;
import com.factory.management.entity.AttendanceStatus;
import com.factory.management.entity.ProductionReportStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeActualStagingResponse {
    Long id;
    Long productionReportStagingId;
    ProductionReportStatus reportStatus;
    Long employeeId; String employeeCode; String employeeName;
    Long employeeTeamId; String employeeTeamCode; String employeeTeamName;
    Integer workingMinutes;
    Integer overtimeMinutes;
    AttendanceStatus attendanceStatus;
    AssignmentType assignmentType;
    String description;
    Boolean active;
}
