package com.factory.management.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

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
