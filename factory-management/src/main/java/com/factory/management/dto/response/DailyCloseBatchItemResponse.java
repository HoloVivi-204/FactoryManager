package com.factory.management.dto.response;

import com.factory.management.entity.ProductionReportStatus;
import lombok.*;

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
