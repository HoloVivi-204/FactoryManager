package com.factory.management.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.factory.management.dto.request.FactoryRequest;
import com.factory.management.dto.request.FactoryUpdateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.FactoryResponse;
import com.factory.management.service.Service.FactoryService;
import jakarta.validation.Valid;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

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
