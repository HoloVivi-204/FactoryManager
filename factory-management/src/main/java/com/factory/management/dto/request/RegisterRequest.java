package com.factory.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    @NotNull(message = "NOT_NULL_USER_EMPLOYEE_ID")
    private Long employeeId;

    @NotBlank(message = "NOT_BLANK_USERNAME")
    @Size(min = 4, max = 100, message = "SIZE_USERNAME")
    private String username;

    @NotBlank(message = "NOT_BLANK_PASSWORD")
    @Size(min = 8, max = 100, message = "SIZE_PASSWORD")
    private String password;
}
