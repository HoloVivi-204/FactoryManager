package com.factory.management.controller;

import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.ExecutiveDashboardResponse;
import com.factory.management.service.ServiceImpl.ExecutiveDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

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
