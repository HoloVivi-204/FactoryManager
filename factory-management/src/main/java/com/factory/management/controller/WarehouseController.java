package com.factory.management.controller;

import com.factory.management.dto.request.WarehouseRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.WarehouseResponse;
import com.factory.management.service.ServiceImpl.WarehouseService;
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
@RequestMapping("${api.prefix}/warehouses")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FINANCE','FACTORY_MANAGER')")
public class WarehouseController {
    private final WarehouseService service;

    @GetMapping("/all")
    ApiResponse<List<WarehouseResponse>> all() {
        return ApiResponse.<List<WarehouseResponse>>builder()
                .result(service.all())
                .build();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    ApiResponse<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) {
        return ApiResponse.<WarehouseResponse>builder()
                .result(service.create(request))
                .build();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    ApiResponse<WarehouseResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody WarehouseRequest request
    ) {
        return ApiResponse.<WarehouseResponse>builder()
                .result(service.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    ApiResponse<Void> delete(@PathVariable Long id) {
        service.delete(id);

        return ApiResponse.<Void>builder()
                .message("Ngừng sử dụng kho thành công")
                .build();
    }
}
