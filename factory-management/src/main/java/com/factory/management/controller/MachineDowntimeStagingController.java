package com.factory.management.controller;

import com.factory.management.dto.request.MachineDowntimeStagingRequest;
import com.factory.management.dto.request.MachineDowntimeStagingUpdateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.MachineDowntimeStagingResponse;
import com.factory.management.service.Service.MachineDowntimeStagingService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
@RequestMapping("${api.prefix}/machine-downtime-staging")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachineDowntimeStagingController {
    MachineDowntimeStagingService service;

    @PreAuthorize("@authorizationScope.canManageStaging(#r.productionReportStagingId)")
    @PostMapping
    ApiResponse<MachineDowntimeStagingResponse> create(@Valid @RequestBody MachineDowntimeStagingRequest r) {
        return one(service.create(r));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    @GetMapping("/all")
    ApiResponse<List<MachineDowntimeStagingResponse>> getAll() {
        return many(service.getAll());
    }

    @PreAuthorize("@authorizationScope.canManageDowntime(#id)")
    @GetMapping("/{id}")
    ApiResponse<MachineDowntimeStagingResponse> getById(@PathVariable Long id) {
        return one(service.getById(id));
    }

    @PreAuthorize("@authorizationScope.canAccessStaging(#reportId)")
    @GetMapping("/report/{reportId}")
    ApiResponse<List<MachineDowntimeStagingResponse>> getByReport(@PathVariable Long reportId) {
        return many(service.getByReportId(reportId));
    }

    @PreAuthorize("@authorizationScope.canAccessMachine(#machineId)")
    @GetMapping("/machine/{machineId}")
    ApiResponse<List<MachineDowntimeStagingResponse>> getByMachine(@PathVariable Long machineId) {
        return many(service.getByMachineId(machineId));
    }

    @PreAuthorize("@authorizationScope.canManageDowntime(#id)")
    @PutMapping("/{id}")
    ApiResponse<MachineDowntimeStagingResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MachineDowntimeStagingUpdateRequest request
    ) {
        return one(service.update(id, request));
    }

    @PreAuthorize("@authorizationScope.canManageDowntime(#id)")
    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        service.delete(id);

        return ApiResponse.<String>builder()
                .message("Xóa dữ liệu dừng máy tạm thành công")
                .build();
    }

    private ApiResponse<MachineDowntimeStagingResponse> one(MachineDowntimeStagingResponse value) {
        return ApiResponse.<MachineDowntimeStagingResponse>builder()
                .result(value)
                .build();
    }

    private ApiResponse<List<MachineDowntimeStagingResponse>> many(List<MachineDowntimeStagingResponse> value) {
        return ApiResponse.<List<MachineDowntimeStagingResponse>>builder()
                .result(value)
                .build();
    }
}
