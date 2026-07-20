package com.factory.management.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductionReportStagingRequest {
    @NotNull(message = "NOT_NULL_REPORT_DATE") LocalDate reportDate;
    @NotNull(message = "NOT_NULL_REPORT_SHIFT_ID") Long shiftId;
    @NotNull(message = "NOT_NULL_REPORT_FACTORY_ID") Long factoryId;
    @NotNull(message = "NOT_NULL_REPORT_DEPARTMENT_ID") Long departmentId;
    @NotNull(message = "NOT_NULL_REPORT_PRODUCTION_LINE_ID") Long productionLineId;
    @NotNull(message = "NOT_NULL_REPORT_TEAM_ID") Long teamId;
    @NotNull(message = "NOT_NULL_REPORT_LEADER_ID") Long leaderEmployeeId;
    @NotNull(message = "NOT_NULL_REPORT_MACHINE_ID") Long machineId;
    @NotNull(message = "NOT_NULL_PLANNED_QUANTITY") @PositiveOrZero(message = "INVALID_REPORT_QUANTITY") Long plannedQuantity;
    @NotNull(message = "NOT_NULL_ACTUAL_QUANTITY") @PositiveOrZero(message = "INVALID_REPORT_QUANTITY") Long actualQuantity;
    @NotNull(message = "NOT_NULL_DEFECT_QUANTITY") @PositiveOrZero(message = "INVALID_REPORT_QUANTITY") Long defectQuantity;
    @NotNull(message = "NOT_NULL_WORKING_MINUTES") @PositiveOrZero(message = "INVALID_REPORT_MINUTES") Integer workingMinutes;
    @NotNull(message = "NOT_NULL_DOWNTIME_MINUTES") @PositiveOrZero(message = "INVALID_REPORT_MINUTES") Integer downtimeMinutes;
    @Size(max = 1000, message = "SIZE_REPORT_NOTE") String note;
}
