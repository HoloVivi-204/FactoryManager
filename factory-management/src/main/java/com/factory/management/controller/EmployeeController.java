package com.factory.management.controller;

import com.factory.management.dto.request.EmployeeRequest;
import com.factory.management.dto.request.EmployeeUpdateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.EmployeeResponse;
import com.factory.management.service.Service.EmployeeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/employees")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeController {
    EmployeeService employeeService;

    @PostMapping
    ApiResponse<EmployeeResponse> create(@Valid @RequestBody EmployeeRequest request) {
        return ApiResponse.<EmployeeResponse>builder()
                .result(employeeService.create(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<EmployeeResponse>> getAll() {
        return ApiResponse.<List<EmployeeResponse>>builder()
                .result(employeeService.getAll())
                .build();
    }

    @GetMapping("/team/{teamId}")
    ApiResponse<List<EmployeeResponse>> getAllByTeamId(@PathVariable Long teamId) {
        return ApiResponse.<List<EmployeeResponse>>builder()
                .result(employeeService.getAllByTeamId(teamId))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<EmployeeResponse> getById(@PathVariable Long id) {
        return ApiResponse.<EmployeeResponse>builder()
                .result(employeeService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<EmployeeResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request
    ) {
        return ApiResponse.<EmployeeResponse>builder()
                .result(employeeService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        employeeService.delete(id);
        return ApiResponse.<String>builder()
                .message("Xóa nhân viên thành công")
                .build();
    }
}
