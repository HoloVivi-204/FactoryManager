package com.factory.management.modules.auth.dto.response;

import com.factory.management.modules.auth.entity.Role;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
