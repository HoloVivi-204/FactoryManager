package com.factory.management.dto.response;

import lombok.*;

import java.time.LocalDateTime;

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
