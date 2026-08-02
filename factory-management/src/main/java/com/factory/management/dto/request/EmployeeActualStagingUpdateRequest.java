package com.factory.management.dto.request;

import com.factory.management.entity.AssignmentType;
import com.factory.management.entity.AttendanceStatus;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
