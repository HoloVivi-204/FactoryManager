package com.factory.management.controller;

import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.ProductionDashboardResponse;
import com.factory.management.dto.response.ProductionReportDetailResponse;
import com.factory.management.dto.response.ProductionReportResponse;
import com.factory.management.service.ProductionReportDetailService;
import com.factory.management.service.ProductionReportService;
import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/production-reports")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductionReportController {
    ProductionReportService service;
    ProductionReportDetailService detailService;

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FINANCE')")
    @GetMapping("/all")
    ApiResponse<List<ProductionReportResponse>> getAll() {
        return many(service.getAll());
    }

    @PreAuthorize("@authorizationScope.canAccessReport(#id)")
    @GetMapping("/{id}")
    ApiResponse<ProductionReportResponse> getById(@PathVariable Long id) {
        return one(service.getById(id));
    }

    @PreAuthorize("@authorizationScope.canAccessReport(#id)")
    @GetMapping("/{id}/details")
    ApiResponse<ProductionReportDetailResponse> getDetails(@PathVariable Long id) {
        return ApiResponse.<ProductionReportDetailResponse>builder()
                .result(detailService.getByReportId(id))
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FINANCE')")
    @GetMapping("/report-no/{reportNo}")
    ApiResponse<ProductionReportResponse> getByReportNo(@PathVariable String reportNo) {
        return one(service.getByReportNo(reportNo));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FINANCE')")
    @GetMapping("/date/{date}")
    ApiResponse<List<ProductionReportResponse>> getByDate(
            @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        return many(service.getByDate(date));
    }

    @PreAuthorize("@authorizationScope.canAccessTeam(#teamId)")
    @GetMapping("/team/{teamId}")
    ApiResponse<List<ProductionReportResponse>> getByTeam(@PathVariable Long teamId) {
        return many(service.getByTeamId(teamId));
    }

    @PreAuthorize("@authorizationScope.canAccessMachine(#machineId)")
    @GetMapping("/machine/{machineId}")
    ApiResponse<List<ProductionReportResponse>> getByMachine(@PathVariable Long machineId) {
        return many(service.getByMachineId(machineId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FINANCE')")
    @GetMapping("/search")
    ApiResponse<List<ProductionReportResponse>> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long factoryId,
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Long machineId
    ) {
        return many(service.search(fromDate, toDate, factoryId, teamId, machineId));
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FINANCE')")
    @GetMapping("/dashboard")
    ApiResponse<ProductionDashboardResponse> dashboard() {
        return ApiResponse.<ProductionDashboardResponse>builder()
                .result(service.getDashboard())
                .build();
    }

    @PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FINANCE','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER','TEAM_LEADER')")
    @GetMapping("/dashboard/my-scope")
    ApiResponse<ProductionDashboardResponse> myDashboard() {
        return ApiResponse.<ProductionDashboardResponse>builder()
                .result(service.getMyDashboard())
                .build();
    }

    private ApiResponse<ProductionReportResponse> one(ProductionReportResponse value) {
        return ApiResponse.<ProductionReportResponse>builder()
                .result(value)
                .build();
    }

    private ApiResponse<List<ProductionReportResponse>> many(List<ProductionReportResponse> value) {
        return ApiResponse.<List<ProductionReportResponse>>builder()
                .result(value)
                .build();
    }
}
