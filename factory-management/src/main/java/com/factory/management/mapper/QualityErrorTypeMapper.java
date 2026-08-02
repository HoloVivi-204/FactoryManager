package com.factory.management.mapper;

import com.factory.management.dto.request.QualityErrorTypeRequest;
import com.factory.management.dto.request.QualityErrorTypeUpdateRequest;
import com.factory.management.dto.response.QualityErrorTypeResponse;
import com.factory.management.entity.QualityErrorType;
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
