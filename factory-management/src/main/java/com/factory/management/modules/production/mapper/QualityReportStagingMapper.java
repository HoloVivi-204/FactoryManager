package com.factory.management.modules.production.mapper;

import com.factory.management.modules.production.dto.response.QualityReportStagingResponse;
import com.factory.management.modules.production.entity.QualityReportStaging;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface QualityReportStagingMapper {
    @Mapping(target = "productionReportStagingId", source = "productionReportStaging.id")
    @Mapping(target = "reportStatus", source = "productionReportStaging.status")
    @Mapping(target = "qualityErrorTypeId", source = "qualityErrorType.id")
    @Mapping(target = "qualityErrorTypeCode", source = "qualityErrorType.code")
    @Mapping(target = "qualityErrorTypeName", source = "qualityErrorType.name")
    @Mapping(target = "severity", source = "qualityErrorType.severity")
    QualityReportStagingResponse mapToResponse(QualityReportStaging value);
}
