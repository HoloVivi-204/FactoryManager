package com.factory.management.dto.response;

import com.factory.management.entity.MachineOperationalStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MachineResponse {
    Long id;
    String code;
    String name;
    String description;
    String serialNumber;
    LocalDate installationDate;
    MachineOperationalStatus status;
    Boolean active;
    Long machineTypeId;
    String machineTypeCode;
    String machineTypeName;
    Long teamId;
    String teamCode;
    String teamName;
    Long productionLineId;
    String productionLineCode;
    Long departmentId;
    String departmentCode;
    Long factoryId;
    String factoryCode;
}
