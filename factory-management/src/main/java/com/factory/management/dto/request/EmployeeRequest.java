package com.factory.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
