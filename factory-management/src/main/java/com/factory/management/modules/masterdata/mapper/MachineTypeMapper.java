package com.factory.management.modules.masterdata.mapper;

import com.factory.management.modules.masterdata.dto.request.MachineTypeRequest;
import com.factory.management.modules.masterdata.dto.request.MachineTypeUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.MachineTypeResponse;
import com.factory.management.modules.masterdata.entity.MachineType;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MachineTypeMapper {
    @Mapping(target = "id", ignore = true)
    MachineType mapToMachineType(MachineTypeRequest request);

    MachineTypeResponse mapToMachineTypeResponse(MachineType machineType);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateMachineTypeFromRequest(
            MachineTypeUpdateRequest request,
            @MappingTarget MachineType machineType
    );
}
