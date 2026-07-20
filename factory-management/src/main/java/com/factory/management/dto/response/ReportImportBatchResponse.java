package com.factory.management.dto.response;

import com.factory.management.entity.ReportImportStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportImportBatchResponse {
    private Long id;
    private Long version;
    private String importNo;
    private Long closeBatchId;
    private String closeBatchNo;
    private Long generatedFileId;
    private Integer generatedFileVersion;
    private ReportImportStatus status;
    private String uploadedFileName;
    private String contentType;
    private Long contentLength;
    private String fileHashSha256;
    private Integer totalRows;
    private Integer successfulRows;
    private Integer errorCount;
    private String failureMessage;
    private LocalDateTime uploadedAt;
    private String uploadedBy;
    private LocalDateTime validatedAt;
    private LocalDateTime importedAt;
    private String importedBy;
}
