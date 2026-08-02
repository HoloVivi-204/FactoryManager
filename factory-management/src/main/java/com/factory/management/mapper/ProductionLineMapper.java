package com.factory.management.mapper;

import com.factory.management.dto.request.ProductionLineRequest;
import com.factory.management.dto.request.ProductionLineUpdateRequest;
import com.factory.management.dto.response.ProductionLineResponse;
import com.factory.management.entity.ProductionLine;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ProductionLineMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    ProductionLine mapToProductionLine(ProductionLineRequest request);

    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentCode", source = "department.code")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "factoryId", source = "department.factory.id")
    @Mapping(target = "factoryCode", source = "department.factory.code")
    @Mapping(target = "factoryName", source = "department.factory.name")
    ProductionLineResponse mapToProductionLineResponse(ProductionLine productionLine);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "department", ignore = true)
    void updateProductionLineFromRequest(
            ProductionLineUpdateRequest request,
            @MappingTarget ProductionLine productionLine
    );
}
