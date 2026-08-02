package com.factory.management.modules.masterdata.controller;

import com.factory.management.modules.masterdata.dto.request.EmployeeRequest;
import com.factory.management.modules.masterdata.dto.request.EmployeeUpdateRequest;
import com.factory.management.common.api.ApiResponse;
import com.factory.management.modules.masterdata.dto.response.EmployeeResponse;
import com.factory.management.modules.masterdata.service.EmployeeService;
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
