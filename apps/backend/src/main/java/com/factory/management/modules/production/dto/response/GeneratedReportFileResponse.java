package com.factory.management.modules.production.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GeneratedReportFileResponse {
    private Long id;
    private Long closeBatchId;
    private Integer fileVersion;
    private String templateVersion;
    private String fileName;
    private String contentType;
    private Long contentLength;
    private String checksumSha256;
    private Integer dataRowCount;
    private LocalDateTime generatedAt;
    private String generatedBy;
}
