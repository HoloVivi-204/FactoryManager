package com.factory.management.dto.response;

import com.factory.management.entity.ProductionReportStatus;
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
public class ProductionReportStagingResponse {
    Long id;
    LocalDate reportDate;
    Long shiftId; String shiftCode; String shiftName;
    Long factoryId; String factoryCode; String factoryName;
    Long departmentId; String departmentCode; String departmentName;
    Long productionLineId; String productionLineCode; String productionLineName;
    Long teamId; String teamCode; String teamName;
    Long leaderEmployeeId; String leaderEmployeeCode; String leaderEmployeeName;
    Long machineId; String machineCode; String machineName;
    Long plannedQuantity;
    Long actualQuantity;
    Long goodQuantity;
    Long defectQuantity;
    Integer workingMinutes;
    Integer downtimeMinutes;
    String note;
    ProductionReportStatus status;
    LocalDateTime submittedAt;
    LocalDateTime reviewedAt;
    String reviewComment;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}
