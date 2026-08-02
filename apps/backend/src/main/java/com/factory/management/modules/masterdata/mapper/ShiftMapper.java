package com.factory.management.modules.masterdata.mapper;

import com.factory.management.modules.masterdata.dto.request.ShiftRequest;
import com.factory.management.modules.masterdata.dto.request.ShiftUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.ShiftResponse;
import com.factory.management.modules.masterdata.entity.Shift;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ShiftMapper {
    @Mapping(target = "id", ignore = true)
    Shift mapToShift(ShiftRequest request);

    ShiftResponse mapToShiftResponse(Shift shift);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateShiftFromRequest(ShiftUpdateRequest request, @MappingTarget Shift shift);
}
