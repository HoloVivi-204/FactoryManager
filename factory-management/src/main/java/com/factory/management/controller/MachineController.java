package com.factory.management.controller;

import com.factory.management.dto.request.MachineRequest;
import com.factory.management.dto.request.MachineUpdateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.MachineResponse;
import com.factory.management.entity.MachineOperationalStatus;
import com.factory.management.service.MachineService;
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
@RequestMapping("${api.prefix}/machines")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachineController {
    MachineService machineService;

    @PostMapping
    ApiResponse<MachineResponse> create(@Valid @RequestBody MachineRequest request) {
        return ApiResponse.<MachineResponse>builder()
                .result(machineService.create(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<MachineResponse>> getAll() {
        return ApiResponse.<List<MachineResponse>>builder()
                .result(machineService.getAll())
                .build();
    }

    @GetMapping("/team/{teamId}")
    ApiResponse<List<MachineResponse>> getAllByTeamId(@PathVariable Long teamId) {
        return ApiResponse.<List<MachineResponse>>builder()
                .result(machineService.getAllByTeamId(teamId))
                .build();
    }

    @GetMapping("/machine-type/{machineTypeId}")
    ApiResponse<List<MachineResponse>> getAllByMachineTypeId(@PathVariable Long machineTypeId) {
        return ApiResponse.<List<MachineResponse>>builder()
                .result(machineService.getAllByMachineTypeId(machineTypeId))
                .build();
    }

    @GetMapping("/status/{status}")
    ApiResponse<List<MachineResponse>> getAllByOperationalStatus(
            @PathVariable("status") MachineOperationalStatus operationalStatus
    ) {
        return ApiResponse.<List<MachineResponse>>builder()
                .result(machineService.getAllByOperationalStatus(operationalStatus))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<MachineResponse> getById(@PathVariable Long id) {
        return ApiResponse.<MachineResponse>builder()
                .result(machineService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<MachineResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody MachineUpdateRequest request
    ) {
        return ApiResponse.<MachineResponse>builder()
                .result(machineService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        machineService.delete(id);
        return ApiResponse.<String>builder()
                .message("Xóa máy thành công")
                .build();
    }
}
