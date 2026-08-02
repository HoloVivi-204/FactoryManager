package com.factory.management.modules.masterdata.mapper;

import com.factory.management.modules.masterdata.dto.request.QualityErrorTypeRequest;
import com.factory.management.modules.masterdata.dto.request.QualityErrorTypeUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.QualityErrorTypeResponse;
import com.factory.management.modules.masterdata.entity.QualityErrorType;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface QualityErrorTypeMapper {
    @Mapping(target = "id", ignore = true)
    QualityErrorType mapToQualityErrorType(QualityErrorTypeRequest request);

    QualityErrorTypeResponse mapToQualityErrorTypeResponse(QualityErrorType qualityErrorType);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateQualityErrorTypeFromRequest(
            QualityErrorTypeUpdateRequest request,
            @MappingTarget QualityErrorType qualityErrorType
    );
}
