package com.factory.management.controller;

import com.factory.management.dto.request.*;
import com.factory.management.dto.response.*;
import com.factory.management.service.Service.EmployeeActualStagingService;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/employee-actual-staging")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeActualStagingController {
    EmployeeActualStagingService service;

    @PreAuthorize("@authorizationScope.canManageStaging(#r.productionReportStagingId)") @PostMapping ApiResponse<EmployeeActualStagingResponse> create(@Valid @RequestBody EmployeeActualStagingRequest r) { return one(service.create(r)); }
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')") @GetMapping("/all") ApiResponse<List<EmployeeActualStagingResponse>> getAll() { return many(service.getAll()); }
    @PreAuthorize("@authorizationScope.canManageEmployeeActual(#id)") @GetMapping("/{id}") ApiResponse<EmployeeActualStagingResponse> getById(@PathVariable Long id) { return one(service.getById(id)); }
    @PreAuthorize("@authorizationScope.canAccessStaging(#reportId)") @GetMapping("/report/{reportId}") ApiResponse<List<EmployeeActualStagingResponse>> getByReport(@PathVariable Long reportId) { return many(service.getByReportId(reportId)); }
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','DEPARTMENT_MANAGER')") @GetMapping("/employee/{employeeId}") ApiResponse<List<EmployeeActualStagingResponse>> getByEmployee(@PathVariable Long employeeId) { return many(service.getByEmployeeId(employeeId)); }
    @PreAuthorize("@authorizationScope.canManageEmployeeActual(#id)") @PutMapping("/{id}") ApiResponse<EmployeeActualStagingResponse> update(@PathVariable Long id, @Valid @RequestBody EmployeeActualStagingUpdateRequest r) { return one(service.update(id, r)); }
    @PreAuthorize("@authorizationScope.canManageEmployeeActual(#id)") @DeleteMapping("/{id}") ApiResponse<String> delete(@PathVariable Long id) { service.delete(id); return ApiResponse.<String>builder().message("Xóa nhân sự thực tế tạm thành công").build(); }

    private ApiResponse<EmployeeActualStagingResponse> one(EmployeeActualStagingResponse value) { return ApiResponse.<EmployeeActualStagingResponse>builder().result(value).build(); }
    private ApiResponse<List<EmployeeActualStagingResponse>> many(List<EmployeeActualStagingResponse> value) { return ApiResponse.<List<EmployeeActualStagingResponse>>builder().result(value).build(); }
}
