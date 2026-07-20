package com.factory.management.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmployeeRequest {
    Long teamId;

    @NotBlank(message = "NOT_BLANK_EMPLOYEE_CODE")
    @Size(max = 50, message = "SIZE_EMPLOYEE_CODE")
    String code;

    @NotBlank(message = "NOT_BLANK_EMPLOYEE_FULL_NAME")
    @Size(max = 255, message = "SIZE_EMPLOYEE_FULL_NAME")
    String fullName;

    @NotBlank(message = "NOT_BLANK_EMPLOYEE_POSITION")
    @Size(max = 255, message = "SIZE_EMPLOYEE_POSITION")
    String position;

    @NotNull(message = "NOT_NULL_EMPLOYEE_HIRE_DATE")
    @PastOrPresent(message = "INVALID_EMPLOYEE_HIRE_DATE")
    LocalDate hireDate;

    Boolean active;
}
