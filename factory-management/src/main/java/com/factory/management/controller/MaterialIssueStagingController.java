package com.factory.management.controller;

import com.factory.management.dto.request.MaterialIssueStagingRequest;
import com.factory.management.dto.request.MaterialIssueStagingUpdateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.MaterialIssueStagingResponse;
import com.factory.management.entity.MaterialIssueType;
import com.factory.management.service.MaterialIssueStagingService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
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
@RequestMapping("${api.prefix}/material-issue-staging")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MaterialIssueStagingController {
    MaterialIssueStagingService service;

    @PreAuthorize("@authorizationScope.canManageStaging(#r.productionReportStagingId)")
    @PostMapping
    ApiResponse<MaterialIssueStagingResponse> create(@Valid @RequestBody MaterialIssueStagingRequest r) {
        return one(service.create(r));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    @GetMapping("/all")
    ApiResponse<List<MaterialIssueStagingResponse>> getAll() {
        return many(service.getAll());
    }

    @PreAuthorize("@authorizationScope.canManageMaterialIssue(#id)")
    @GetMapping("/{id}")
    ApiResponse<MaterialIssueStagingResponse> getById(@PathVariable Long id) {
        return one(service.getById(id));
    }

    @PreAuthorize("@authorizationScope.canAccessStaging(#reportId)")
    @GetMapping("/report/{reportId}")
    ApiResponse<List<MaterialIssueStagingResponse>> getByReport(@PathVariable Long reportId) {
        return many(service.getByReportId(reportId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    @GetMapping("/material/{materialId}")
    ApiResponse<List<MaterialIssueStagingResponse>> getByMaterial(@PathVariable Long materialId) {
        return many(service.getByMaterialId(materialId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
    @GetMapping("/type/{issueType}")
    ApiResponse<List<MaterialIssueStagingResponse>> getByType(@PathVariable MaterialIssueType issueType) {
        return many(service.getByIssueType(issueType));
    }

    @PreAuthorize("@authorizationScope.canManageMaterialIssue(#id)")
    @PutMapping("/{id}")
    ApiResponse<MaterialIssueStagingResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MaterialIssueStagingUpdateRequest request
    ) {
        return one(service.update(id, request));
    }

    @PreAuthorize("@authorizationScope.canManageMaterialIssue(#id)")
    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        service.delete(id);

        return ApiResponse.<String>builder()
                .message("Xóa dữ liệu sự cố vật tư tạm thành công")
                .build();
    }

    private ApiResponse<MaterialIssueStagingResponse> one(MaterialIssueStagingResponse value) {
        return ApiResponse.<MaterialIssueStagingResponse>builder()
                .result(value)
                .build();
    }

    private ApiResponse<List<MaterialIssueStagingResponse>> many(List<MaterialIssueStagingResponse> value) {
        return ApiResponse.<List<MaterialIssueStagingResponse>>builder()
                .result(value)
                .build();
    }
}
