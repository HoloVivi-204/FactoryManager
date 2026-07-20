package com.factory.management.controller;

import com.factory.management.dto.request.MachineTypeRequest;
import com.factory.management.dto.request.MachineTypeUpdateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.MachineTypeResponse;
import com.factory.management.service.Service.MachineTypeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/machine-types")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachineTypeController {
    MachineTypeService machineTypeService;

    @PostMapping
    ApiResponse<MachineTypeResponse> create(@Valid @RequestBody MachineTypeRequest request) {
        return ApiResponse.<MachineTypeResponse>builder()
                .result(machineTypeService.create(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<MachineTypeResponse>> getAll() {
        return ApiResponse.<List<MachineTypeResponse>>builder()
                .result(machineTypeService.getAll())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<MachineTypeResponse> getById(@PathVariable Long id) {
        return ApiResponse.<MachineTypeResponse>builder()
                .result(machineTypeService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<MachineTypeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MachineTypeUpdateRequest request
    ) {
        return ApiResponse.<MachineTypeResponse>builder()
                .result(machineTypeService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        machineTypeService.delete(id);
        return ApiResponse.<String>builder()
                .message("Xóa loại máy thành công")
                .build();
    }
}
