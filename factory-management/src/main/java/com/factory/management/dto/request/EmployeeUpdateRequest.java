package com.factory.management.dto.request;

import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeUpdateRequest {
    Long teamId;
    Boolean removeFromTeam;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_EMPLOYEE_CODE")
    @Size(max = 50, message = "SIZE_EMPLOYEE_CODE")
    String code;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_EMPLOYEE_FULL_NAME")
    @Size(max = 255, message = "SIZE_EMPLOYEE_FULL_NAME")
    String fullName;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_EMPLOYEE_POSITION")
    @Size(max = 255, message = "SIZE_EMPLOYEE_POSITION")
    String position;

    @PastOrPresent(message = "INVALID_EMPLOYEE_HIRE_DATE")
    LocalDate hireDate;

    Boolean active;
}
