package com.factory.management.controller;

import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.AuditEventResponse;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.service.impl.AuditService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/audit-events")
@PreAuthorize("hasRole('ADMIN')")
public class AuditController {

    private final AuditService service;

    @GetMapping
    ApiResponse<PageResponse<AuditEventResponse>> search(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
            @RequestParam(required = false) String actor,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) String entityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ApiResponse.<PageResponse<AuditEventResponse>>builder()
                .result(service.search(from, to, actor, action, entityType, page, size))
                .build();
    }
}
