package com.factory.management.modules.production.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingExcelImportErrorResponse {
    private String sheet;
    private int row;
    private String column;
    private String code;
    private String message;
    private String value;
}
