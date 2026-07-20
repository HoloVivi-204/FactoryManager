package com.factory.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class AuditEventResponse {
    private Long id;
    private String actorUsername;
    private Long actorEmployeeId;
    private String action;
    private String entityType;
    private String entityId;
    private String requestId;
    private String ipAddress;
    private String detailsJson;
    private LocalDateTime occurredAt;
}
