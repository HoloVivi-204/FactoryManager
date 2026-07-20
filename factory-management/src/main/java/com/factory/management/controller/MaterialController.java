package com.factory.management.controller;

import com.factory.management.dto.request.MaterialRequest;
import com.factory.management.dto.request.MaterialUpdateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.MaterialResponse;
import com.factory.management.service.Service.MaterialService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
