package com.factory.management.dto.request;
import jakarta.validation.constraints.*;import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder public class RegisterRequest {
 @NotNull(message="NOT_NULL_USER_EMPLOYEE_ID") Long employeeId;
 @NotBlank(message="NOT_BLANK_USERNAME") @Size(min=4,max=100,message="SIZE_USERNAME") String username;
 @NotBlank(message="NOT_BLANK_PASSWORD") @Size(min=8,max=100,message="SIZE_PASSWORD") String password;
}
