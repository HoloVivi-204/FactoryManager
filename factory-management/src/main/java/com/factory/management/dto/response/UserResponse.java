package com.factory.management.dto.response;

import com.factory.management.entity.Role;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class UserResponse {
    private Long id;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String username;
    private Set<Role> roles;
    private Boolean enabled;
    private Boolean accountNonLocked;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
}
