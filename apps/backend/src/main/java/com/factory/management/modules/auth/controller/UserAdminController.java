package com.factory.management.modules.auth.controller;

import com.factory.management.modules.auth.dto.request.ResetPasswordRequest;
import com.factory.management.modules.auth.dto.request.UpdateUserRolesRequest;
import com.factory.management.common.api.ApiResponse;
import com.factory.management.modules.auth.dto.response.UserResponse;
import com.factory.management.modules.auth.service.UserManagementService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserAdminController {
    private final UserManagementService service;

    @GetMapping("/all")
    ApiResponse<List<UserResponse>> getAll() {
        return ApiResponse.<List<UserResponse>>builder().result(service.getAll()).build();
    }

    @GetMapping("/{id}")
    ApiResponse<UserResponse> getById(@PathVariable Long id) {
        return one(service.getById(id));
    }

    @PutMapping("/{id}/roles")
    ApiResponse<UserResponse> updateRoles(@PathVariable Long id,
                                          @Valid @RequestBody UpdateUserRolesRequest request) {
        return one(service.updateRoles(id, request));
    }

    @PutMapping("/{id}/enabled/{enabled}")
    ApiResponse<UserResponse> setEnabled(@PathVariable Long id, @PathVariable boolean enabled) {
        return one(service.setEnabled(id, enabled));
    }

    @PutMapping("/{id}/locked/{locked}")
    ApiResponse<UserResponse> setLocked(@PathVariable Long id, @PathVariable boolean locked) {
        return one(service.setAccountNonLocked(id, !locked));
    }

    @PutMapping("/{id}/reset-password")
    ApiResponse<Void> resetPassword(@PathVariable Long id,
                                    @Valid @RequestBody ResetPasswordRequest request) {
        service.resetPassword(id, request);
        return ApiResponse.<Void>builder().message("Đặt lại mật khẩu thành công").build();
    }

    private ApiResponse<UserResponse> one(UserResponse value) {
        return ApiResponse.<UserResponse>builder().result(value).build();
    }
}
