package com.factory.management.controller;

import com.factory.management.dto.request.ProductionPlanRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.dto.response.ProductionPlanResponse;
import com.factory.management.entity.ProductionPlanStatus;
import com.factory.management.service.impl.ProductionPlanService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
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
@RequestMapping("${api.prefix}/production-plans")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER','TEAM_LEADER')")
public class ProductionPlanController {

    private final ProductionPlanService service;

    @GetMapping
    public ApiResponse<PageResponse<ProductionPlanResponse>> search(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long factoryId,
            @RequestParam(required = false) Long productionLineId,
            @RequestParam(required = false) Long productId,
            @RequestParam(required = false) ProductionPlanStatus status,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.search(fromDate, toDate, factoryId, productionLineId,
                productId, status, active, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductionPlanResponse> get(@PathVariable Long id) {
        return ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<ProductionPlanResponse> create(@Valid @RequestBody ProductionPlanRequest request) {
        return ok(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<ProductionPlanResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductionPlanRequest request
    ) {
        return ok(service.update(id, request));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<ProductionPlanResponse> approve(@PathVariable Long id) {
        return ApiResponse.<ProductionPlanResponse>builder()
                .message("Đã phê duyệt kế hoạch sản xuất")
                .result(service.approve(id))
                .build();
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<ProductionPlanResponse> close(@PathVariable Long id) {
        return ApiResponse.<ProductionPlanResponse>builder()
                .message("Đã đóng kế hoạch sản xuất")
                .result(service.close(id))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.<Void>builder().message("Đã ngừng kế hoạch sản xuất nháp").build();
    }

    private <T> ApiResponse<T> ok(T value) {
        return ApiResponse.<T>builder().result(value).build();
    }
}
