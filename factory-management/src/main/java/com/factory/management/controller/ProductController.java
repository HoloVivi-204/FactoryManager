package com.factory.management.controller;

import com.factory.management.dto.request.ProductRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.dto.response.ProductResponse;
import com.factory.management.service.impl.ProductService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/products")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER','TEAM_LEADER')")
public class ProductController {

    private final ProductService service;

    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.search(keyword, active, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductResponse> get(@PathVariable Long id) {
        return ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<ProductResponse> create(@Valid @RequestBody ProductRequest request) {
        return ok(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest request
    ) {
        return ok(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.<Void>builder().message("Đã ngừng sử dụng sản phẩm").build();
    }

    private <T> ApiResponse<T> ok(T value) {
        return ApiResponse.<T>builder().result(value).build();
    }
}
