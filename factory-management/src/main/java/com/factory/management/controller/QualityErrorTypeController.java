package com.factory.management.controller;

import com.factory.management.dto.request.QualityErrorTypeRequest;
import com.factory.management.dto.request.QualityErrorTypeUpdateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.QualityErrorTypeResponse;
import com.factory.management.entity.QualityErrorSeverity;
import com.factory.management.service.Service.QualityErrorTypeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/quality-error-types")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QualityErrorTypeController {
    QualityErrorTypeService qualityErrorTypeService;

    @PostMapping
    ApiResponse<QualityErrorTypeResponse> create(@Valid @RequestBody QualityErrorTypeRequest request) {
        return ApiResponse.<QualityErrorTypeResponse>builder()
                .result(qualityErrorTypeService.create(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<QualityErrorTypeResponse>> getAll() {
        return ApiResponse.<List<QualityErrorTypeResponse>>builder()
                .result(qualityErrorTypeService.getAll())
                .build();
    }

    @GetMapping("/severity/{severity}")
    ApiResponse<List<QualityErrorTypeResponse>> getAllBySeverity(
            @PathVariable QualityErrorSeverity severity
    ) {
        return ApiResponse.<List<QualityErrorTypeResponse>>builder()
                .result(qualityErrorTypeService.getAllBySeverity(severity))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<QualityErrorTypeResponse> getById(@PathVariable Long id) {
        return ApiResponse.<QualityErrorTypeResponse>builder()
                .result(qualityErrorTypeService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<QualityErrorTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody QualityErrorTypeUpdateRequest request
    ) {
        return ApiResponse.<QualityErrorTypeResponse>builder()
                .result(qualityErrorTypeService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        qualityErrorTypeService.delete(id);
        return ApiResponse.<String>builder()
                .message("Xóa loại lỗi chất lượng thành công")
                .build();
    }
}
