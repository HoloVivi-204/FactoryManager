package com.factory.management.modules.dashboard.controller;

import com.factory.management.common.api.ApiResponse;
import com.factory.management.modules.dashboard.dto.response.ExecutiveDashboardResponse;
import com.factory.management.modules.dashboard.service.ExecutiveDashboardService;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/executive-dashboard")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR')")
public class ExecutiveDashboardController {
    private final ExecutiveDashboardService executiveDashboardService;

    @GetMapping
    public ApiResponse<ExecutiveDashboardResponse> getDashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long factoryId
    ) {
        return ApiResponse.<ExecutiveDashboardResponse>builder()
                .result(executiveDashboardService.getDashboard(fromDate, toDate, factoryId))
                .build();
    }
}
