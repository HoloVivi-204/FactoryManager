package com.factory.management.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "daily_close_batch", uniqueConstraints = {
        @UniqueConstraint(name = "uk_daily_close_batch_no", columnNames = "batch_no"),
        @UniqueConstraint(name = "uk_daily_close_business_key", columnNames = "business_key")
}, indexes = {
        @Index(name = "idx_daily_close_date_scope", columnList = "report_date,scope_type,scope_id"),
        @Index(name = "idx_daily_close_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCloseBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "batch_no", nullable = false, length = 80)
    private String batchNo;

    @Column(name = "business_key", nullable = false, length = 160)
    private String businessKey;

    @Column(name = "report_date", nullable = false)
    private LocalDate reportDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 30)
    private DataScopeType scopeType;

    @Column(name = "scope_id", nullable = false)
    private Long scopeId;

    @Column(name = "scope_code", nullable = false, length = 100)
    private String scopeCode;

    @Column(name = "scope_name", nullable = false, length = 255)
    private String scopeName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_id")
    private Shift shift;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private DailyCloseBatchStatus status;

    @Column(name = "report_count", nullable = false)
    private Integer reportCount;

    @Column(name = "latest_file_version", nullable = false)
    @Builder.Default
    private Integer latestFileVersion = 0;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false, updatable = false, length = 100)
    private String createdBy;

    @Column(name = "closed_at", nullable = false, updatable = false)
    private LocalDateTime closedAt;

    @Column(name = "closed_by", nullable = false, updatable = false, length = 100)
    private String closedBy;

    @Column(name = "generated_at")
    private LocalDateTime generatedAt;

    @Column(name = "generated_by", length = 100)
    private String generatedBy;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    @Column(name = "imported_by", length = 100)
    private String importedBy;

    @Column(name = "locked_at")
    private LocalDateTime lockedAt;

    @Column(name = "locked_by", length = 100)
    private String lockedBy;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void create() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) createdAt = now;
        if (closedAt == null) closedAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void update() {
        updatedAt = LocalDateTime.now();
    }
}
