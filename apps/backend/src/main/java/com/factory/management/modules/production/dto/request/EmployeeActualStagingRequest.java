package com.factory.management.modules.production.dto.request;

import com.factory.management.modules.masterdata.entity.AssignmentType;
import com.factory.management.modules.hr.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
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
public class EmployeeActualStagingRequest {
    @NotNull(message = "NOT_NULL_EMPLOYEE_ACTUAL_REPORT_ID") Long productionReportStagingId;
    @NotNull(message = "NOT_NULL_EMPLOYEE_ACTUAL_EMPLOYEE_ID") Long employeeId;
    @NotNull(message = "NOT_NULL_EMPLOYEE_ACTUAL_WORKING_MINUTES")
    @PositiveOrZero(message = "INVALID_EMPLOYEE_ACTUAL_MINUTES") Integer workingMinutes;
    @NotNull(message = "NOT_NULL_EMPLOYEE_ACTUAL_OVERTIME_MINUTES")
    @PositiveOrZero(message = "INVALID_EMPLOYEE_ACTUAL_MINUTES") Integer overtimeMinutes;
    @NotNull(message = "NOT_NULL_ATTENDANCE_STATUS") AttendanceStatus attendanceStatus;
    @NotNull(message = "NOT_NULL_ASSIGNMENT_TYPE") AssignmentType assignmentType;
    @Size(max = 1000, message = "SIZE_EMPLOYEE_ACTUAL_DESCRIPTION") String description;
    Boolean active;
}
