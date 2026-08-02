package com.factory.management.controller;

import com.factory.management.dto.request.FinancialCategoryRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.FinancialCategoryResponse;
import com.factory.management.service.ServiceImpl.FinancialCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/financial-categories")
public class FinancialCategoryController {
    private final FinancialCategoryService service;

    @GetMapping("/all")
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FINANCE','FACTORY_MANAGER')")
    ApiResponse<List<FinancialCategoryResponse>> all() {
        return ApiResponse.<List<FinancialCategoryResponse>>builder()
                .result(service.all())
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    ApiResponse<FinancialCategoryResponse> create(@Valid @RequestBody FinancialCategoryRequest request) {
        return ApiResponse.<FinancialCategoryResponse>builder()
                .result(service.create(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    ApiResponse<FinancialCategoryResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FinancialCategoryRequest request
    ) {
        return ApiResponse.<FinancialCategoryResponse>builder()
                .result(service.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);

        return ApiResponse.<Void>builder()
                .message("Ngừng sử dụng danh mục tài chính thành công")
                .build();
    }
}
