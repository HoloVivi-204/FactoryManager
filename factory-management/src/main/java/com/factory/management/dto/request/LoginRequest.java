package com.factory.management.dto.request;
import jakarta.validation.constraints.NotBlank;import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder public class LoginRequest {@NotBlank(message="NOT_BLANK_USERNAME") String username;@NotBlank(message="NOT_BLANK_PASSWORD") String password;}
