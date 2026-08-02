package com.factory.management.modules.production.mapper;

import com.factory.management.modules.production.dto.response.MaterialIssueStagingResponse;
import com.factory.management.modules.production.entity.MaterialIssueStaging;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MaterialIssueStagingMapper {
    @Mapping(target = "productionReportStagingId", source = "productionReportStaging.id")
    @Mapping(target = "reportStatus", source = "productionReportStaging.status")
    @Mapping(target = "materialId", source = "material.id")
    @Mapping(target = "materialCode", source = "material.code")
    @Mapping(target = "materialName", source = "material.name")
    MaterialIssueStagingResponse mapToResponse(MaterialIssueStaging value);
}
