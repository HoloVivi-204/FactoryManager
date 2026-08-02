package com.factory.management.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "generated_report_file", uniqueConstraints = {
        @UniqueConstraint(name = "uk_generated_file_batch_version", columnNames = {"close_batch_id", "file_version"}),
        @UniqueConstraint(name = "uk_generated_file_checksum", columnNames = {"close_batch_id", "checksum_sha256"})
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedReportFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "close_batch_id", nullable = false, updatable = false)
    private DailyCloseBatch closeBatch;

    @Column(name = "file_version", nullable = false)
    private Integer fileVersion;

    @Column(name = "template_version", nullable = false, length = 30)
    private String templateVersion;

    @Column(name = "file_name", nullable = false, length = 255)
    private String fileName;

    @Column(name = "content_type", nullable = false, length = 100)
    private String contentType;

    @Column(name = "content_length", nullable = false)
    private Long contentLength;

    @Column(name = "checksum_sha256", nullable = false, length = 64)
    private String checksumSha256;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file_content", nullable = false, columnDefinition = "bytea")
    private byte[] content;

    @Column(name = "data_row_count", nullable = false)
    private Integer dataRowCount;

    @Column(name = "generated_at", nullable = false, updatable = false)
    private LocalDateTime generatedAt;

    @Column(name = "generated_by", nullable = false, updatable = false, length = 100)
    private String generatedBy;
}
