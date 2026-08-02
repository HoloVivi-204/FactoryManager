package com.factory.management.controller;

import com.factory.management.dto.request.MaintenancePartUsageRequest;
import com.factory.management.dto.request.MaintenanceRequestCreateRequest;
import com.factory.management.dto.request.MaintenanceRequestStatusRequest;
import com.factory.management.dto.request.MaintenanceScheduleRequest;
import com.factory.management.dto.request.MaintenanceWorkOrderRequest;
import com.factory.management.dto.request.MaintenanceWorkOrderStatusRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.MaintenanceResponse;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.entity.MaintenancePriority;
import com.factory.management.entity.MaintenanceRequestStatus;
import com.factory.management.entity.MaintenanceWorkOrderStatus;
import com.factory.management.service.ServiceImpl.MaintenanceService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
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
@RequestMapping("${api.prefix}/maintenance")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER','TEAM_LEADER','FINANCE')")
public class MaintenanceController {

    private final MaintenanceService service;

    @GetMapping("/options/machines")
    public ApiResponse<List<MaintenanceResponse.MachineOption>> machineOptions() {
        return ok(service.machineOptions());
    }

    @GetMapping("/requests")
    public ApiResponse<PageResponse<MaintenanceResponse.RequestItem>> requests(
            @RequestParam(required = false) Long machineId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) MaintenanceRequestStatus status,
            @RequestParam(required = false) MaintenancePriority priority,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.requests(machineId, teamId, status, priority, keyword, fromDate, toDate, page, size));
    }

    @GetMapping("/requests/{id}")
    public ApiResponse<MaintenanceResponse.RequestItem> request(@PathVariable Long id) {
        return ok(service.request(id));
    }

    @PostMapping("/requests")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER','TEAM_LEADER')")
    public ApiResponse<MaintenanceResponse.RequestItem> createRequest(
            @Valid @RequestBody MaintenanceRequestCreateRequest request
    ) {
        return ok(service.createRequest(request));
    }

    @PutMapping("/requests/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<MaintenanceResponse.RequestItem> updateRequestStatus(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceRequestStatusRequest request
    ) {
        return ok(service.updateRequestStatus(id, request));
    }

    @GetMapping("/schedules")
    public ApiResponse<PageResponse<MaintenanceResponse.ScheduleItem>> schedules(
            @RequestParam(required = false) Long machineId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dueBefore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.schedules(machineId, teamId, active, dueBefore, page, size));
    }

    @GetMapping("/schedules/{id}")
    public ApiResponse<MaintenanceResponse.ScheduleItem> schedule(@PathVariable Long id) {
        return ok(service.schedule(id));
    }

    @PostMapping("/schedules")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<MaintenanceResponse.ScheduleItem> createSchedule(
            @Valid @RequestBody MaintenanceScheduleRequest request
    ) {
        return ok(service.saveSchedule(null, request));
    }

    @PutMapping("/schedules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<MaintenanceResponse.ScheduleItem> updateSchedule(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceScheduleRequest request
    ) {
        return ok(service.saveSchedule(id, request));
    }

    @DeleteMapping("/schedules/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<Void> deleteSchedule(@PathVariable Long id) {
        service.deleteSchedule(id);
        return message("Đã ngừng lịch bảo trì");
    }

    @GetMapping("/work-orders")
    public ApiResponse<PageResponse<MaintenanceResponse.WorkOrderItem>> workOrders(
            @RequestParam(required = false) Long machineId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) MaintenanceWorkOrderStatus status,
            @RequestParam(required = false) MaintenancePriority priority,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.workOrders(machineId, teamId, status, priority, from, to, page, size));
    }

    @GetMapping("/work-orders/{id}")
    public ApiResponse<MaintenanceResponse.WorkOrderItem> workOrder(@PathVariable Long id) {
        return ok(service.workOrder(id));
    }

    @PostMapping("/work-orders")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<MaintenanceResponse.WorkOrderItem> createWorkOrder(
            @Valid @RequestBody MaintenanceWorkOrderRequest request
    ) {
        return ok(service.createWorkOrder(request));
    }

    @PutMapping("/work-orders/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<MaintenanceResponse.WorkOrderItem> updateWorkOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody MaintenanceWorkOrderStatusRequest request
    ) {
        return ok(service.updateWorkOrderStatus(id, request));
    }

    @PostMapping("/work-orders/{id}/parts")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<MaintenanceResponse.PartUsageItem> addPart(
            @PathVariable Long id,
            @Valid @RequestBody MaintenancePartUsageRequest request
    ) {
        return ok(service.addPart(id, request));
    }

    @DeleteMapping("/work-orders/{workOrderId}/parts/{partId}")
    @PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER')")
    public ApiResponse<Void> deletePart(@PathVariable Long workOrderId, @PathVariable Long partId) {
        service.deletePart(workOrderId, partId);
        return message("Đã bỏ vật tư khỏi phiếu bảo trì");
    }

    @GetMapping("/machines/{machineId}/status-history")
    public ApiResponse<PageResponse<MaintenanceResponse.StatusHistoryItem>> machineHistory(
            @PathVariable Long machineId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.machineHistory(machineId, page, size));
    }

    @GetMapping("/dashboard")
    public ApiResponse<MaintenanceResponse.Dashboard> dashboard(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate
    ) {
        return ok(service.dashboard(teamId, fromDate, toDate));
    }

    private <T> ApiResponse<T> ok(T value) {
        return ApiResponse.<T>builder().result(value).build();
    }

    private ApiResponse<Void> message(String value) {
        return ApiResponse.<Void>builder().message(value).build();
    }
}
