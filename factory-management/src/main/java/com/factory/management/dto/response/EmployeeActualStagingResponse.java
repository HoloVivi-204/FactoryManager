package com.factory.management.dto.response;

import com.factory.management.entity.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
