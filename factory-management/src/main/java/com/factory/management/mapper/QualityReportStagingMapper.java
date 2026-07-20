package com.factory.management.mapper;

import com.factory.management.dto.response.QualityReportStagingResponse;
import com.factory.management.entity.QualityReportStaging;
import org.mapstruct.*;

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
