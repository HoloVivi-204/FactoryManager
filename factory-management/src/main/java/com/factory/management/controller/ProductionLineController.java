package com.factory.management.controller;

import com.factory.management.dto.request.ProductionLineRequest;
import com.factory.management.dto.request.ProductionLineUpdateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.ProductionLineResponse;
import com.factory.management.service.ProductionLineService;
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
@RequestMapping("${api.prefix}/production-lines")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductionLineController {
    ProductionLineService productionLineService;

    @PostMapping
    ApiResponse<ProductionLineResponse> create(@Valid @RequestBody ProductionLineRequest request) {
        return ApiResponse.<ProductionLineResponse>builder()
                .result(productionLineService.create(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<ProductionLineResponse>> getAll() {
        return ApiResponse.<List<ProductionLineResponse>>builder()
                .result(productionLineService.getAll())
                .build();
    }

    @GetMapping("/department/{departmentId}")
    ApiResponse<List<ProductionLineResponse>> getAllByDepartmentId(@PathVariable Long departmentId) {
        return ApiResponse.<List<ProductionLineResponse>>builder()
                .result(productionLineService.getAllByDepartmentId(departmentId))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ProductionLineResponse> getById(@PathVariable Long id) {
        return ApiResponse.<ProductionLineResponse>builder()
                .result(productionLineService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<ProductionLineResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductionLineUpdateRequest request
    ) {
        return ApiResponse.<ProductionLineResponse>builder()
                .result(productionLineService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        productionLineService.delete(id);
        return ApiResponse.<String>builder()
                .message("Xóa dây chuyền sản xuất thành công")
                .build();
    }
}
