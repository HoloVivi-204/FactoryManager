package com.factory.management.controller;

import com.factory.management.dto.request.*;
import com.factory.management.dto.response.*;
import com.factory.management.service.ServiceImpl.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/auth")
public class AuthController {
    private final AuthService service;

    @PostMapping("/register")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ok(service.register(request));
    }

    @PostMapping("/login")
    ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ok(service.authenticate(request));
    }

    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@Valid @RequestBody IntrospectRequest request) {
        return ApiResponse.<IntrospectResponse>builder().result(service.introspect(request)).build();
    }

    @PostMapping("/refresh")
    ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ok(service.refresh(request));
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@Valid @RequestBody LogoutRequest request) {
        service.logout(request);
        return ApiResponse.<Void>builder().message("Đăng xuất thành công").build();
    }

    @GetMapping("/me")
    ApiResponse<AuthResponse> me(@AuthenticationPrincipal Jwt jwt) {
        return ok(service.me(jwt.getSubject()));
    }

    @PutMapping("/change-password")
    ApiResponse<Void> changePassword(@AuthenticationPrincipal Jwt jwt,
                                     @Valid @RequestBody ChangePasswordRequest request) {
        service.changePassword(jwt.getSubject(), request);
        return ApiResponse.<Void>builder()
                .message("Đổi mật khẩu thành công, vui lòng đăng nhập lại").build();
    }

    private ApiResponse<AuthResponse> ok(AuthResponse value) {
        return ApiResponse.<AuthResponse>builder().result(value).build();
    }
}
