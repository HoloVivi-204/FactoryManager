package com.factory.management.dto.response;

import com.factory.management.entity.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDateTime;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MachineDowntimeStagingResponse {
    Long id;
    Long productionReportStagingId;
    ProductionReportStatus reportStatus;
    Long machineId; String machineCode; String machineName;
    Long downtimeReasonId; String downtimeReasonCode; String downtimeReasonName;
    DowntimeReasonType downtimeReasonType;
    LocalDateTime startTime;
    LocalDateTime endTime;
    Integer durationMinutes;
    String description;
    Boolean active;
}
