package com.factory.management.modules.production.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductionReportResponse {
    Long id; String reportNo; LocalDate reportDate; Long sourceStagingId;
    Long shiftId; String shiftCode; String shiftName;
    Long factoryId; String factoryCode; String factoryName;
    Long departmentId; String departmentCode; String departmentName;
    Long productionLineId; String productionLineCode; String productionLineName;
    Long teamId; String teamCode; String teamName;
    Long leaderEmployeeId; String leaderEmployeeCode; String leaderEmployeeName;
    Long machineId; String machineCode; String machineName;
    Long plannedQuantity; Long actualQuantity; Long goodQuantity; Long defectQuantity;
    Integer workingMinutes; Integer downtimeMinutes;
    BigDecimal availability; BigDecimal performance; BigDecimal quality; BigDecimal oee;
    String remark; LocalDateTime approvedAt;
    Long approvedById; String approvedByCode; String approvedByName;
    LocalDateTime createdAt; Long createdById; String createdByCode; String createdByName;
}
