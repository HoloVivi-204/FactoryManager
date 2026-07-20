package com.factory.management.controller;

import com.factory.management.dto.request.ProductionOrderRequest;
import com.factory.management.dto.request.ProductionOrderStatusRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.dto.response.ProductionOrderResponse;
import com.factory.management.entity.ProductionOrderStatus;
import com.factory.management.service.ServiceImpl.ProductionOrderService;
import jakarta.validation.Valid;
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

import java.time.LocalDate;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/production-orders")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER','TEAM_LEADER')")
public class ProductionOrderController {

    private final ProductionOrderService service;

    @GetMapping
    public ApiResponse<PageResponse<ProductionOrderResponse>> search(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long productionPlanId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long machineId,
            @RequestParam(required = false) Long shiftId,
            @RequestParam(required = false) ProductionOrderStatus status,
            @RequestParam(required = false) Boolean active,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.search(fromDate, toDate, productionPlanId, teamId,
                machineId, shiftId, status, active, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<ProductionOrderResponse> get(@PathVariable Long id) {
        return ok(service.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<ProductionOrderResponse> create(@Valid @RequestBody ProductionOrderRequest request) {
        return ok(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<ProductionOrderResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductionOrderRequest request
    ) {
        return ok(service.update(id, request));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<ProductionOrderResponse> changeStatus(
            @PathVariable Long id,
            @Valid @RequestBody ProductionOrderStatusRequest request
    ) {
        return ApiResponse.<ProductionOrderResponse>builder()
                .message("Đã cập nhật trạng thái lệnh sản xuất")
                .result(service.changeStatus(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.<Void>builder().message("Đã ngừng lệnh sản xuất nháp").build();
    }

    private <T> ApiResponse<T> ok(T value) {
        return ApiResponse.<T>builder().result(value).build();
    }
}
