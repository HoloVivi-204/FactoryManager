package com.factory.management.dto.request;

import com.factory.management.entity.*;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeActualStagingUpdateRequest {
    Long productionReportStagingId;
    Long employeeId;
    @PositiveOrZero(message = "INVALID_EMPLOYEE_ACTUAL_MINUTES") Integer workingMinutes;
    @PositiveOrZero(message = "INVALID_EMPLOYEE_ACTUAL_MINUTES") Integer overtimeMinutes;
    AttendanceStatus attendanceStatus;
    AssignmentType assignmentType;
    @Size(max = 1000, message = "SIZE_EMPLOYEE_ACTUAL_DESCRIPTION") String description;
    Boolean active;
}
