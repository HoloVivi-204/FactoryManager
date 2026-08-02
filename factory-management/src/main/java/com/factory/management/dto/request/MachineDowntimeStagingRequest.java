package com.factory.management.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
public class MachineDowntimeStagingRequest {
    @NotNull(message = "NOT_NULL_DOWNTIME_REPORT_ID") Long productionReportStagingId;
    @NotNull(message = "NOT_NULL_DOWNTIME_MACHINE_ID") Long machineId;
    @NotNull(message = "NOT_NULL_DOWNTIME_REASON_ID") Long downtimeReasonId;
    @NotNull(message = "NOT_NULL_DOWNTIME_START_TIME") LocalDateTime startTime;
    @NotNull(message = "NOT_NULL_DOWNTIME_END_TIME") LocalDateTime endTime;
    @Size(max = 1000, message = "SIZE_DOWNTIME_DESCRIPTION") String description;
    Boolean active;
}
