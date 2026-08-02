package com.factory.management.modules.production.dto.response;

import com.factory.management.modules.production.entity.ProductionReportStatus;
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
public class DailyCloseBatchItemResponse {
    private Long id;
    private Integer sequenceNo;
    private Long stagingReportId;
    private ProductionReportStatus stagingStatus;
    private Long officialReportId;
    private String sourceHash;
    private Long shiftId;
    private String shiftCode;
    private Long factoryId;
    private String factoryCode;
    private Long departmentId;
    private String departmentCode;
    private Long productionLineId;
    private String productionLineCode;
    private Long teamId;
    private String teamCode;
    private Long machineId;
    private String machineCode;
}
