package com.factory.management.controller;

import com.factory.management.dto.request.*;
import com.factory.management.dto.response.*;
import com.factory.management.entity.ProductionReportStatus;
import com.factory.management.service.Service.ProductionReportStagingService;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/production-report-staging")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductionReportStagingController {
    ProductionReportStagingService service;

    @PreAuthorize("@authorizationScope.canManageTeam(#r.teamId)")
    @PostMapping ApiResponse<ProductionReportStagingResponse> create(@Valid @RequestBody ProductionReportStagingRequest r) { return response(service.create(r)); }
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')") @GetMapping("/all") ApiResponse<List<ProductionReportStagingResponse>> getAll() { return list(service.getAll()); }
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER','TEAM_LEADER')")
    @GetMapping("/my-scope") ApiResponse<List<ProductionReportStagingResponse>> getMyScope() { return list(service.getMyScope()); }
    @PreAuthorize("@authorizationScope.canAccessStaging(#id)") @GetMapping("/{id}") ApiResponse<ProductionReportStagingResponse> getById(@PathVariable Long id) { return response(service.getById(id)); }
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')") @GetMapping("/date/{date}") ApiResponse<List<ProductionReportStagingResponse>> getByDate(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) { return list(service.getByDate(date)); }
    @PreAuthorize("@authorizationScope.canAccessTeam(#teamId)") @GetMapping("/team/{teamId}") ApiResponse<List<ProductionReportStagingResponse>> getByTeam(@PathVariable Long teamId) { return list(service.getByTeamId(teamId)); }
    @PreAuthorize("@authorizationScope.canAccessMachine(#machineId)") @GetMapping("/machine/{machineId}") ApiResponse<List<ProductionReportStagingResponse>> getByMachine(@PathVariable Long machineId) { return list(service.getByMachineId(machineId)); }
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')") @GetMapping("/status/{status}") ApiResponse<List<ProductionReportStagingResponse>> getByStatus(@PathVariable ProductionReportStatus status) { return list(service.getByStatus(status)); }
    @PreAuthorize("@authorizationScope.canManageStaging(#id)")
    @PutMapping("/{id}") ApiResponse<ProductionReportStagingResponse> update(@PathVariable Long id, @Valid @RequestBody ProductionReportStagingUpdateRequest r) { return response(service.update(id, r)); }
    @PreAuthorize("@authorizationScope.canManageStaging(#id)")
    @PutMapping("/{id}/submit") ApiResponse<ProductionReportStagingResponse> submit(@PathVariable Long id) { return response(service.submit(id)); }
    @PreAuthorize("@authorizationScope.canApproveStaging(#id)")
    @PutMapping("/{id}/request-change") ApiResponse<ProductionReportStagingResponse> requestChange(@PathVariable Long id, @Valid @RequestBody ProductionReportReviewRequest request) { return response(service.requestChange(id, request)); }
    @PreAuthorize("@authorizationScope.canManageStaging(#id)")
    @PutMapping("/{id}/return-to-draft") ApiResponse<ProductionReportStagingResponse> returnToDraft(@PathVariable Long id) { return response(service.returnToDraft(id)); }
    @PreAuthorize("@authorizationScope.canApproveStaging(#id)")
    @PostMapping("/{id}/approve")
    ApiResponse<ProductionReportResponse> approve(
            @PathVariable Long id,
            @Valid @RequestBody ProductionReportApprovalRequest request
    ) { return ApiResponse.<ProductionReportResponse>builder().result(service.approve(id, request)).build(); }
    @PreAuthorize("@authorizationScope.canApproveStaging(#id)")
    @PutMapping("/{id}/lock") ApiResponse<ProductionReportStagingResponse> lock(@PathVariable Long id) { return response(service.lock(id)); }
    @PreAuthorize("@authorizationScope.canManageStaging(#id)")
    @DeleteMapping("/{id}") ApiResponse<Void> deleteDraft(@PathVariable Long id) {
        service.deleteDraft(id);
        return ApiResponse.<Void>builder().message("Đã xóa báo cáo nháp và toàn bộ dữ liệu chi tiết").build();
    }

    private ApiResponse<ProductionReportStagingResponse> response(ProductionReportStagingResponse value) { return ApiResponse.<ProductionReportStagingResponse>builder().result(value).build(); }
    private ApiResponse<List<ProductionReportStagingResponse>> list(List<ProductionReportStagingResponse> value) { return ApiResponse.<List<ProductionReportStagingResponse>>builder().result(value).build(); }
}
