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
