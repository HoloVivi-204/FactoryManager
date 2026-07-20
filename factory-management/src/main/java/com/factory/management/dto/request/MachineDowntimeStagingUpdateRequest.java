package com.factory.management.dto.request;

import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

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
