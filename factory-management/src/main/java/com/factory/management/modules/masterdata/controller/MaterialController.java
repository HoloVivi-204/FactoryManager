package com.factory.management.modules.masterdata.controller;

import com.factory.management.modules.masterdata.dto.request.MaterialRequest;
import com.factory.management.modules.masterdata.dto.request.MaterialUpdateRequest;
import com.factory.management.common.api.ApiResponse;
import com.factory.management.modules.masterdata.dto.response.MaterialResponse;
import com.factory.management.modules.masterdata.service.MaterialService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
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
@RequestMapping("${api.prefix}/materials")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MaterialController {
    MaterialService materialService;

    @PostMapping
    ApiResponse<MaterialResponse> create(@Valid @RequestBody MaterialRequest request) {
        return ApiResponse.<MaterialResponse>builder()
                .result(materialService.create(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<MaterialResponse>> getAll() {
        return ApiResponse.<List<MaterialResponse>>builder()
                .result(materialService.getAll())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<MaterialResponse> getById(@PathVariable Long id) {
        return ApiResponse.<MaterialResponse>builder()
                .result(materialService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<MaterialResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MaterialUpdateRequest request
    ) {
        return ApiResponse.<MaterialResponse>builder()
                .result(materialService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        materialService.delete(id);
        return ApiResponse.<String>builder()
                .message("Xóa nguyên vật liệu thành công")
                .build();
    }
}
