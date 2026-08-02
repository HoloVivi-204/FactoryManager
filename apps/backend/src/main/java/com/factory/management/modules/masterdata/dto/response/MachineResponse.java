package com.factory.management.modules.masterdata.dto.response;

import com.factory.management.modules.maintenance.entity.MachineOperationalStatus;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
