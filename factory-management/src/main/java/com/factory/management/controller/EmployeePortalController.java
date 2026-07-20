package com.factory.management.controller;

import com.factory.management.dto.request.CreateLeaveRequest;
import com.factory.management.dto.request.OvertimeCreateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.EmployeePortalResponse;
import com.factory.management.dto.response.HrManagementResponse;
import com.factory.management.service.ServiceImpl.EmployeePortalService;
import com.factory.management.service.ServiceImpl.HrManagementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/employee-portal")
public class EmployeePortalController {
    private final EmployeePortalService service;
    private final HrManagementService hrManagementService;

    @GetMapping("/dashboard")
    ApiResponse<EmployeePortalResponse> dashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ApiResponse.<EmployeePortalResponse>builder()
                .result(service.dashboard(fromDate, toDate)).build();
    }

    @PostMapping("/leave-requests")
    ApiResponse<EmployeePortalResponse.LeaveItem> leave(@Valid @RequestBody CreateLeaveRequest request) {
        return ApiResponse.<EmployeePortalResponse.LeaveItem>builder()
                .result(service.createLeave(request)).build();
    }

    @GetMapping("/overtime-requests")
    ApiResponse<List<HrManagementResponse.OvertimeItem>> overtime() {
        return ApiResponse.<List<HrManagementResponse.OvertimeItem>>builder()
                .result(hrManagementService.ownOvertime()).build();
    }

    @PostMapping("/overtime-requests")
    ApiResponse<HrManagementResponse.OvertimeItem> overtime(
            @Valid @RequestBody OvertimeCreateRequest request
    ) {
        return ApiResponse.<HrManagementResponse.OvertimeItem>builder()
                .result(hrManagementService.createOwnOvertime(request)).build();
    }

    @PutMapping("/notifications/{id}/read")
    ApiResponse<Void> read(@PathVariable Long id) {
        service.markRead(id);
        return ApiResponse.<Void>builder().message("Đã đọc thông báo").build();
    }
}
