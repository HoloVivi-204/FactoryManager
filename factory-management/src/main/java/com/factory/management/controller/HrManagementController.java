package com.factory.management.controller;

import com.factory.management.dto.request.AttendanceUpsertRequest;
import com.factory.management.dto.request.EmployeeAssignmentRequest;
import com.factory.management.dto.request.EmployeeKpiRequest;
import com.factory.management.dto.request.LeaveDecisionRequest;
import com.factory.management.dto.request.NotificationCreateRequest;
import com.factory.management.dto.request.OvertimeCreateRequest;
import com.factory.management.dto.request.OvertimeDecisionRequest;
import com.factory.management.dto.request.WorkScheduleRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.HrManagementResponse;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.entity.AttendanceStatus;
import com.factory.management.entity.LeaveStatus;
import com.factory.management.entity.OvertimeStatus;
import com.factory.management.service.impl.HrManagementService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/hr")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER','TEAM_LEADER')")
public class HrManagementController {

    private final HrManagementService service;

    @GetMapping("/schedules")
    ApiResponse<PageResponse<HrManagementResponse.ScheduleItem>> schedules(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.schedules(fromDate, toDate, employeeId, teamId, page, size));
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    ApiResponse<HrManagementResponse.ScheduleItem> createSchedule(@Valid @RequestBody WorkScheduleRequest request) {
        return ok(service.createSchedule(request));
    }

    @PutMapping("/schedules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    ApiResponse<HrManagementResponse.ScheduleItem> updateSchedule(
            @PathVariable Long id, @Valid @RequestBody WorkScheduleRequest request
    ) {
        return ok(service.updateSchedule(id, request));
    }

    @DeleteMapping("/schedules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    ApiResponse<Void> deleteSchedule(@PathVariable Long id) {
        service.deleteSchedule(id);
        return message("Đã ngừng lịch làm việc");
    }

    @GetMapping("/attendance")
    ApiResponse<PageResponse<HrManagementResponse.AttendanceItem>> attendance(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) AttendanceStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.attendance(fromDate, toDate, employeeId, teamId, status, page, size));
    }

    @PutMapping("/attendance")
    ApiResponse<HrManagementResponse.AttendanceItem> upsertAttendance(
            @Valid @RequestBody AttendanceUpsertRequest request
    ) {
        return ok(service.upsertAttendance(request));
    }

    @GetMapping("/leave-requests")
    ApiResponse<PageResponse<HrManagementResponse.LeaveItem>> leaveRequests(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) LeaveStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.leaveRequests(fromDate, toDate, employeeId, teamId, status, page, size));
    }

    @PutMapping("/leave-requests/{id}/decision")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER')")
    ApiResponse<HrManagementResponse.LeaveItem> decideLeave(
            @PathVariable Long id, @Valid @RequestBody LeaveDecisionRequest request
    ) {
        return ok(service.decideLeave(id, request));
    }

    @GetMapping("/kpis")
    ApiResponse<PageResponse<HrManagementResponse.KpiItem>> kpis(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.kpis(fromDate, toDate, employeeId, teamId, page, size));
    }

    @PostMapping("/kpis")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    ApiResponse<HrManagementResponse.KpiItem> createKpi(@Valid @RequestBody EmployeeKpiRequest request) {
        return ok(service.saveKpi(null, request));
    }

    @PutMapping("/kpis/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    ApiResponse<HrManagementResponse.KpiItem> updateKpi(
            @PathVariable Long id, @Valid @RequestBody EmployeeKpiRequest request
    ) {
        return ok(service.saveKpi(id, request));
    }

    @PostMapping("/notifications")
    ApiResponse<HrManagementResponse.NotificationItem> createNotification(
            @Valid @RequestBody NotificationCreateRequest request
    ) {
        return ok(service.createNotification(request));
    }

    @GetMapping("/overtime-requests")
    ApiResponse<PageResponse<HrManagementResponse.OvertimeItem>> overtime(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) OvertimeStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.overtime(fromDate, toDate, employeeId, teamId, status, page, size));
    }

    @PostMapping("/overtime-requests")
    ApiResponse<HrManagementResponse.OvertimeItem> createOvertime(
            @Valid @RequestBody OvertimeCreateRequest request
    ) {
        return ok(service.createOvertime(request));
    }

    @PutMapping("/overtime-requests/{id}/decision")
    ApiResponse<HrManagementResponse.OvertimeItem> decideOvertime(
            @PathVariable Long id, @Valid @RequestBody OvertimeDecisionRequest request
    ) {
        return ok(service.decideOvertime(id, request));
    }

    @GetMapping("/assignments")
    ApiResponse<PageResponse<HrManagementResponse.AssignmentItem>> assignments(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.assignments(fromDate, toDate, employeeId, teamId, page, size));
    }

    @PostMapping("/assignments")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    ApiResponse<HrManagementResponse.AssignmentItem> createAssignment(
            @Valid @RequestBody EmployeeAssignmentRequest request
    ) {
        return ok(service.createAssignment(request));
    }

    @PutMapping("/assignments/{id}/end")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    ApiResponse<Void> endAssignment(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate
    ) {
        service.endAssignment(id, endDate);
        return message("Đã kết thúc điều chuyển nhân sự");
    }

    private <T> ApiResponse<T> ok(T value) {
        return ApiResponse.<T>builder().result(value).build();
    }

    private ApiResponse<Void> message(String value) {
        return ApiResponse.<Void>builder().message(value).build();
    }
}
