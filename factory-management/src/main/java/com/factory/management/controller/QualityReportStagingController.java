package com.factory.management.controller;

import com.factory.management.dto.request.*;
import com.factory.management.dto.response.*;
import com.factory.management.service.Service.QualityReportStagingService;
import jakarta.validation.Valid;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/quality-report-staging")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QualityReportStagingController {
    QualityReportStagingService service;

    @PreAuthorize("@authorizationScope.canManageStaging(#r.productionReportStagingId)") @PostMapping ApiResponse<QualityReportStagingResponse> create(@Valid @RequestBody QualityReportStagingRequest r) { return one(service.create(r)); }
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')") @GetMapping("/all") ApiResponse<List<QualityReportStagingResponse>> getAll() { return many(service.getAll()); }
    @PreAuthorize("@authorizationScope.canManageQuality(#id)") @GetMapping("/{id}") ApiResponse<QualityReportStagingResponse> getById(@PathVariable Long id) { return one(service.getById(id)); }
    @PreAuthorize("@authorizationScope.canAccessStaging(#reportId)") @GetMapping("/report/{reportId}") ApiResponse<List<QualityReportStagingResponse>> getByReport(@PathVariable Long reportId) { return many(service.getByReportId(reportId)); }
    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')") @GetMapping("/error-type/{errorTypeId}") ApiResponse<List<QualityReportStagingResponse>> getByErrorType(@PathVariable Long errorTypeId) { return many(service.getByErrorTypeId(errorTypeId)); }
    @PreAuthorize("@authorizationScope.canManageQuality(#id)") @PutMapping("/{id}") ApiResponse<QualityReportStagingResponse> update(@PathVariable Long id, @Valid @RequestBody QualityReportStagingUpdateRequest r) { return one(service.update(id, r)); }
    @PreAuthorize("@authorizationScope.canManageQuality(#id)") @DeleteMapping("/{id}") ApiResponse<String> delete(@PathVariable Long id) { service.delete(id); return ApiResponse.<String>builder().message("Xóa dữ liệu lỗi chất lượng tạm thành công").build(); }

    private ApiResponse<QualityReportStagingResponse> one(QualityReportStagingResponse value) { return ApiResponse.<QualityReportStagingResponse>builder().result(value).build(); }
    private ApiResponse<List<QualityReportStagingResponse>> many(List<QualityReportStagingResponse> value) { return ApiResponse.<List<QualityReportStagingResponse>>builder().result(value).build(); }
}
