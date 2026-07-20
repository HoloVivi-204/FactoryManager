package com.factory.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class RefreshRequest {
    @NotBlank(message = "NOT_BLANK_TOKEN")
    private String token;
}
