package com.factory.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Append-only business audit event. Audit records must never be updated or
 * soft-deleted because they are used for reconciliation and incident review.
 */
@Entity
@Table(name = "audit_event", indexes = {
        @Index(name = "idx_audit_event_occurred_at", columnList = "occurred_at"),
        @Index(name = "idx_audit_event_actor", columnList = "actor_username,occurred_at"),
        @Index(name = "idx_audit_event_entity", columnList = "entity_type,entity_id,occurred_at"),
        @Index(name = "idx_audit_event_action", columnList = "action,occurred_at")
})
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AuditEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "actor_username", nullable = false, length = 100)
    private String actorUsername;

    @Column(name = "actor_employee_id")
    private Long actorEmployeeId;

    @Column(nullable = false, length = 80)
    private String action;

    @Column(name = "entity_type", nullable = false, length = 100)
    private String entityType;

    @Column(name = "entity_id", length = 100)
    private String entityId;

    @Column(name = "request_id", length = 80)
    private String requestId;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "details_json", columnDefinition = "text")
    private String detailsJson;

    @Column(name = "occurred_at", nullable = false, updatable = false)
    private LocalDateTime occurredAt;
}
