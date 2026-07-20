package com.factory.management.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ChangePasswordRequest {
    @NotBlank(message = "NOT_BLANK_PASSWORD")
    private String currentPassword;

    @NotBlank(message = "NOT_BLANK_PASSWORD")
    @Size(min = 8, max = 100, message = "SIZE_PASSWORD")
    private String newPassword;
}
