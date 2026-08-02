package com.factory.management.modules.finance.controller;

import com.factory.management.modules.finance.dto.request.FinancialCategoryRequest;
import com.factory.management.common.api.ApiResponse;
import com.factory.management.modules.finance.dto.response.FinancialCategoryResponse;
import com.factory.management.modules.finance.service.FinancialCategoryService;
import jakarta.validation.Valid;
import java.util.List;
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
