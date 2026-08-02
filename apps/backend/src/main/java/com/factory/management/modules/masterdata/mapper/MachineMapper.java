package com.factory.management.modules.masterdata.mapper;

import com.factory.management.modules.masterdata.dto.request.MachineRequest;
import com.factory.management.modules.masterdata.dto.request.MachineUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.MachineResponse;
import com.factory.management.modules.masterdata.entity.Machine;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MachineMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "machineType", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "operationalStatus", source = "status")
    Machine mapToMachine(MachineRequest request);

    @Mapping(target = "machineTypeId", source = "machineType.id")
    @Mapping(target = "machineTypeCode", source = "machineType.code")
    @Mapping(target = "machineTypeName", source = "machineType.name")
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamCode", source = "team.code")
    @Mapping(target = "teamName", source = "team.name")
    @Mapping(target = "productionLineId", source = "team.productionLine.id")
    @Mapping(target = "productionLineCode", source = "team.productionLine.code")
    @Mapping(target = "departmentId", source = "team.productionLine.department.id")
    @Mapping(target = "departmentCode", source = "team.productionLine.department.code")
    @Mapping(target = "factoryId", source = "team.productionLine.department.factory.id")
    @Mapping(target = "factoryCode", source = "team.productionLine.department.factory.code")
    @Mapping(target = "status", source = "operationalStatus")
    MachineResponse mapToMachineResponse(Machine machine);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "machineType", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "operationalStatus", source = "status")
    void updateMachineFromRequest(MachineUpdateRequest request, @MappingTarget Machine machine);
}
