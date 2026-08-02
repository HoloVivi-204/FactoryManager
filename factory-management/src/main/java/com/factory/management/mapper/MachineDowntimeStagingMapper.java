package com.factory.management.mapper;

import com.factory.management.dto.response.MachineDowntimeStagingResponse;
import com.factory.management.entity.MachineDowntimeStaging;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MachineDowntimeStagingMapper {
    @Mapping(target = "productionReportStagingId", source = "productionReportStaging.id")
    @Mapping(target = "reportStatus", source = "productionReportStaging.status")
    @Mapping(target = "machineId", source = "machine.id")
    @Mapping(target = "machineCode", source = "machine.code")
    @Mapping(target = "machineName", source = "machine.name")
    @Mapping(target = "downtimeReasonId", source = "downtimeReason.id")
    @Mapping(target = "downtimeReasonCode", source = "downtimeReason.code")
    @Mapping(target = "downtimeReasonName", source = "downtimeReason.name")
    @Mapping(target = "downtimeReasonType", source = "downtimeReason.reasonType")
    MachineDowntimeStagingResponse mapToResponse(MachineDowntimeStaging downtime);
}
