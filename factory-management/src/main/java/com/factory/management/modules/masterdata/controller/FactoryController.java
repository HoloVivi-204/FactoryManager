package com.factory.management.modules.masterdata.controller;

import com.factory.management.modules.masterdata.dto.request.FactoryRequest;
import com.factory.management.modules.masterdata.dto.request.FactoryUpdateRequest;
import com.factory.management.common.api.ApiResponse;
import com.factory.management.modules.masterdata.dto.response.FactoryResponse;
import com.factory.management.modules.masterdata.service.FactoryService;
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
@RequestMapping("${api.prefix}/factories")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FactoryController {
    FactoryService factoryService;

    @PostMapping
    ApiResponse<FactoryResponse> create(@Valid @RequestBody FactoryRequest request) {
        return ApiResponse.<FactoryResponse>builder()
                .result(factoryService.create(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<FactoryResponse>> getAll() {
        return ApiResponse.<List<FactoryResponse>>builder()
                .result(factoryService.getAll())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<FactoryResponse> getById(@PathVariable Long id) {
        return ApiResponse.<FactoryResponse>builder()
                .result(factoryService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<FactoryResponse> update(@PathVariable Long id, @Valid @RequestBody FactoryUpdateRequest request) {
        return ApiResponse.<FactoryResponse>builder()
                .result(factoryService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        factoryService.delete(id);
        return ApiResponse.<String>builder()
                .message("Xóa xưởng thành công")
                .build();
    }
}
