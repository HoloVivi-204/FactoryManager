package com.factory.management.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "NOT_BLANK_PASSWORD")
    private String currentPassword;

    @NotBlank(message = "NOT_BLANK_PASSWORD")
    @Size(min = 8, max = 100, message = "SIZE_PASSWORD")
    private String newPassword;
}
