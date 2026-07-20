package com.factory.management.mapper;

import com.factory.management.dto.request.MachineTypeRequest;
import com.factory.management.dto.request.MachineTypeUpdateRequest;
import com.factory.management.dto.response.MachineTypeResponse;
import com.factory.management.entity.MachineType;
import org.mapstruct.*;

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
