package com.factory.management.controller;

import com.factory.management.dto.request.DepartmentRequest;
import com.factory.management.dto.request.DepartmentUpdateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.DepartmentResponse;
import com.factory.management.dto.response.DepartmentTypeResponse;
import com.factory.management.service.DepartmentService;
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
@RequestMapping("${api.prefix}/departments")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DepartmentController {
    DepartmentService departmentService;

    @PostMapping
    ApiResponse<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.<DepartmentResponse>builder()
                .result(departmentService.create(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<DepartmentResponse>> getAll() {
        return ApiResponse.<List<DepartmentResponse>>builder()
                .result(departmentService.getAll())
                .build();
    }

    @GetMapping("/factory/{factoryId}")
    ApiResponse<List<DepartmentResponse>> getAllByFactoryId(@PathVariable Long factoryId) {
        return ApiResponse.<List<DepartmentResponse>>builder()
                .result(departmentService.getAllByFactoryId(factoryId))
                .build();
    }

    @GetMapping("/types")
    ApiResponse<List<DepartmentTypeResponse>> getTypes() {
        return ApiResponse.<List<DepartmentTypeResponse>>builder()
                .result(departmentService.getTypes())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<DepartmentResponse> getById(@PathVariable Long id) {
        return ApiResponse.<DepartmentResponse>builder()
                .result(departmentService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<DepartmentResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentUpdateRequest request
    ) {
        return ApiResponse.<DepartmentResponse>builder()
                .result(departmentService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.<String>builder()
                .message("Xóa phòng ban thành công")
                .build();
    }
}
