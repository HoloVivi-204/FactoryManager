package com.factory.management.entity;

import jakarta.persistence.Basic;
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
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "report_import_batch", uniqueConstraints = {
        @UniqueConstraint(name = "uk_report_import_no", columnNames = "import_no"),
        @UniqueConstraint(name = "uk_report_import_file_hash", columnNames = {"close_batch_id", "file_hash_sha256"})
}, indexes = {
        @Index(name = "idx_report_import_batch_status", columnList = "status"),
        @Index(name = "idx_report_import_close_batch", columnList = "close_batch_id,uploaded_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportImportBatch {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    private Long version;

    @Column(name = "import_no", nullable = false, length = 80)
    private String importNo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "close_batch_id", nullable = false, updatable = false)
    private DailyCloseBatch closeBatch;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "generated_file_id", nullable = false, updatable = false)
    private GeneratedReportFile generatedFile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ReportImportStatus status;

    @Column(name = "uploaded_file_name", nullable = false, length = 255)
    private String uploadedFileName;

    @Column(name = "content_type", length = 100)
    private String contentType;

    @Column(name = "content_length", nullable = false)
    private Long contentLength;

    @Column(name = "file_hash_sha256", nullable = false, length = 64)
    private String fileHashSha256;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "uploaded_content", nullable = false, columnDefinition = "bytea")
    private byte[] uploadedContent;

    @Column(name = "total_rows", nullable = false)
    @Builder.Default
    private Integer totalRows = 0;

    @Column(name = "successful_rows", nullable = false)
    @Builder.Default
    private Integer successfulRows = 0;

    @Column(name = "error_count", nullable = false)
    @Builder.Default
    private Integer errorCount = 0;

    @Column(name = "failure_message", length = 2000)
    private String failureMessage;

    @Column(name = "uploaded_at", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @Column(name = "uploaded_by", nullable = false, updatable = false, length = 100)
    private String uploadedBy;

    @Column(name = "validated_at")
    private LocalDateTime validatedAt;

    @Column(name = "imported_at")
    private LocalDateTime importedAt;

    @Column(name = "imported_by", length = 100)
    private String importedBy;
}
