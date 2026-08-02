package com.factory.management.modules.production.dto.request;

import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductionReportStagingUpdateRequest {
    LocalDate reportDate;
    Long shiftId;
    Long factoryId;
    Long departmentId;
    Long productionLineId;
    Long teamId;
    Long leaderEmployeeId;
    Long machineId;
    @PositiveOrZero(message = "INVALID_REPORT_QUANTITY") Long plannedQuantity;
    @PositiveOrZero(message = "INVALID_REPORT_QUANTITY") Long actualQuantity;
    @PositiveOrZero(message = "INVALID_REPORT_QUANTITY") Long defectQuantity;
    @PositiveOrZero(message = "INVALID_REPORT_MINUTES") Integer workingMinutes;
    @PositiveOrZero(message = "INVALID_REPORT_MINUTES") Integer downtimeMinutes;
    @Size(max = 1000, message = "SIZE_REPORT_NOTE") String note;
}
