package com.factory.management.dto.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportImportRowErrorResponse {
    private Long id;
    private Long importBatchId;
    private String sheetName;
    private Integer rowNumber;
    private String columnName;
    private String errorCode;
    private String message;
    private String rawValue;
    private LocalDateTime createdAt;
}
