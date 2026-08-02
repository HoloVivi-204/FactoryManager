package com.factory.management.dto.response;

import com.factory.management.entity.DowntimeReasonType;
import com.factory.management.entity.ProductionReportStatus;
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
