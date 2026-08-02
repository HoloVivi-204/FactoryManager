package com.factory.management.controller;

import com.factory.management.dto.request.UserDataScopeRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.UserDataScopeResponse;
import com.factory.management.service.ServiceImpl.UserDataScopeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/users/{userId}/data-scopes")
@PreAuthorize("hasRole('ADMIN')")
public class UserDataScopeController {
    private final UserDataScopeService service;

    @GetMapping
    ApiResponse<List<UserDataScopeResponse>> all(@PathVariable Long userId) {
        return ApiResponse.<List<UserDataScopeResponse>>builder()
                .result(service.getAll(userId))
                .build();
    }

    @PostMapping
    ApiResponse<UserDataScopeResponse> add(
            @PathVariable Long userId,
            @Valid @RequestBody UserDataScopeRequest request
    ) {
        return ApiResponse.<UserDataScopeResponse>builder()
                .result(service.add(userId, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<Void> delete(@PathVariable Long userId, @PathVariable Long id) {
        service.delete(userId, id);
        return ApiResponse.<Void>builder()
                .message("Xóa phạm vi dữ liệu thành công")
                .build();
    }
}
