package com.factory.management.dto.request;

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
public class MachineDowntimeStagingUpdateRequest {
    Long productionReportStagingId;
    Long machineId;
    Long downtimeReasonId;
    LocalDateTime startTime;
    LocalDateTime endTime;
    @Size(max = 1000, message = "SIZE_DOWNTIME_DESCRIPTION") String description;
    Boolean active;
}
