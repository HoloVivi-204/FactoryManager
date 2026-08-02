package com.factory.management.modules.masterdata.dto.request;

import com.factory.management.modules.masterdata.entity.AssignmentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeAssignmentRequest {
    @NotNull(message = "NOT_NULL_ASSIGNMENT_EMPLOYEE_ID")
    private Long employeeId;

    @NotNull(message = "NOT_NULL_ASSIGNMENT_TARGET_TEAM_ID")
    private Long targetTeamId;

    @NotNull(message = "NOT_NULL_ASSIGNMENT_TYPE")
    private AssignmentType assignmentType;

    @NotNull(message = "NOT_NULL_ASSIGNMENT_FROM_DATE")
    private LocalDate effectiveFrom;

    private LocalDate effectiveTo;

    @NotBlank(message = "NOT_BLANK_ASSIGNMENT_REASON")
    @Size(max = 1000, message = "SIZE_ASSIGNMENT_REASON")
    private String reason;
}
