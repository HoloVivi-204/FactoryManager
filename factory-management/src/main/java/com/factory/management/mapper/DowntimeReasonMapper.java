package com.factory.management.mapper;

import com.factory.management.dto.request.DowntimeReasonRequest;
import com.factory.management.dto.request.DowntimeReasonUpdateRequest;
import com.factory.management.dto.response.DowntimeReasonResponse;
import com.factory.management.entity.DowntimeReason;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface DowntimeReasonMapper {
    @Mapping(target = "id", ignore = true)
    DowntimeReason mapToDowntimeReason(DowntimeReasonRequest request);

    DowntimeReasonResponse mapToDowntimeReasonResponse(DowntimeReason downtimeReason);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateDowntimeReasonFromRequest(
            DowntimeReasonUpdateRequest request,
            @MappingTarget DowntimeReason downtimeReason
    );
}
