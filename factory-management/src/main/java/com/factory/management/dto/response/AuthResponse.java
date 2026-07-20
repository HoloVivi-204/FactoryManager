package com.factory.management.dto.response;

import com.factory.management.entity.Role;
import lombok.*;

import java.util.Set;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class AuthResponse {
    private String token;
    private boolean authenticated;
    private long expiresIn;
    private Long userId;
    private Long employeeId;
    private String employeeCode;
    private String employeeName;
    private String username;
    private Set<Role> roles;
}
