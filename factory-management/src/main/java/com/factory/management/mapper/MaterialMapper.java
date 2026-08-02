package com.factory.management.mapper;

import com.factory.management.dto.request.MaterialRequest;
import com.factory.management.dto.request.MaterialUpdateRequest;
import com.factory.management.dto.response.MaterialResponse;
import com.factory.management.entity.Material;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface MaterialMapper {
    @Mapping(target = "id", ignore = true)
    Material mapToMaterial(MaterialRequest request);

    MaterialResponse mapToMaterialResponse(Material material);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    void updateMaterialFromRequest(MaterialUpdateRequest request, @MappingTarget Material material);
}
