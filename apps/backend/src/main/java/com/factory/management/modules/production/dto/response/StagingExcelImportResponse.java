package com.factory.management.modules.production.dto.response;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StagingExcelImportResponse {
    private String fileName;
    private boolean valid;
    private boolean imported;
    private int totalRows;
    private int productionRows;
    private int employeeRows;
    private int downtimeRows;
    private int qualityRows;
    private int materialRows;
    private int createdReports;
    private int updatedReports;
    private int createdDetails;
    private int updatedDetails;
    @Builder.Default
    private List<StagingExcelImportErrorResponse> errors = new ArrayList<>();
}
