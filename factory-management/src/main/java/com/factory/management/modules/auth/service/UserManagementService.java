package com.factory.management.modules.auth.service;

import com.factory.management.common.error.AppException;
import com.factory.management.common.error.ErrorCode;
import com.factory.management.modules.audit.service.AuditService;
import com.factory.management.modules.auth.dto.request.ResetPasswordRequest;
import com.factory.management.modules.auth.dto.request.UpdateUserRolesRequest;
import com.factory.management.modules.auth.dto.response.UserResponse;
import com.factory.management.modules.auth.entity.User;
import com.factory.management.modules.auth.repository.UserRepository;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserManagementService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<UserResponse> getAll() {
        return userRepository.findAll().stream().map(this::response).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getById(Long id) {
        return response(find(id));
    }

    @Transactional
    public UserResponse updateRoles(Long id, UpdateUserRolesRequest request) {
        User user = find(id);
        user.setRoles(request.getRoles());
        increaseTokenVersion(user);
        String roles = request.getRoles().stream()
                .map(role -> "\"" + role.name() + "\"")
                .sorted()
                .collect(Collectors.joining(","));
        auditService.record("USER_ROLES_UPDATED", "USER", user.getId(),
                "{\"roles\":[" + roles + "]}");
        return response(user);
    }

    @Transactional
    public UserResponse setEnabled(Long id, boolean enabled) {
        User user = find(id);
        user.setEnabled(enabled);
        increaseTokenVersion(user);
        auditService.record("USER_ENABLED_CHANGED", "USER", user.getId(),
                "{\"enabled\":" + enabled + "}");
        return response(user);
    }

    @Transactional
    public UserResponse setAccountNonLocked(Long id, boolean nonLocked) {
        User user = find(id);
        user.setAccountNonLocked(nonLocked);
        increaseTokenVersion(user);
        auditService.record("USER_LOCK_CHANGED", "USER", user.getId(),
                "{\"locked\":" + !nonLocked + "}");
        return response(user);
    }

    @Transactional
    public void resetPassword(Long id, ResetPasswordRequest request) {
        User user = find(id);
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        increaseTokenVersion(user);
        auditService.record("USER_PASSWORD_RESET", "USER", user.getId(),
                "{\"credentialChanged\":true}");
    }

    private void increaseTokenVersion(User user) {
        user.setTokenVersion(user.getTokenVersion() + 1);
    }

    private User find(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
    }

    private UserResponse response(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .employeeId(user.getEmployee().getId())
                .employeeCode(user.getEmployee().getCode())
                .employeeName(user.getEmployee().getFullName())
                .username(user.getUsername())
                .roles(Set.copyOf(user.getRoles()))
                .enabled(user.getEnabled())
                .accountNonLocked(user.getAccountNonLocked())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
