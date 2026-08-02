package com.factory.management.modules.production.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "report_import_row_error", indexes = {
        @Index(name = "idx_import_row_error_batch", columnList = "import_batch_id,sheet_name,row_number")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportImportRowError {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "import_batch_id", nullable = false, updatable = false)
    private ReportImportBatch importBatch;

    @Column(name = "sheet_name", nullable = false, length = 100)
    private String sheetName;

    @Column(name = "row_number")
    private Integer rowNumber;

    @Column(name = "column_name", length = 100)
    private String columnName;

    @Column(name = "error_code", nullable = false, length = 80)
    private String errorCode;

    @Column(nullable = false, length = 1000)
    private String message;

    @Column(name = "raw_value", length = 1000)
    private String rawValue;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void create() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
