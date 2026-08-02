package com.factory.management.modules.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class LoginRequest {
    @NotBlank(message = "NOT_BLANK_USERNAME")
    private String username;

    @NotBlank(message = "NOT_BLANK_PASSWORD")
    private String password;
}
