package com.factory.management.mapper;

import com.factory.management.dto.request.FactoryRequest;
import com.factory.management.dto.request.FactoryUpdateRequest;
import com.factory.management.dto.response.FactoryResponse;
import com.factory.management.entity.Factory;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface FactoryMapper {
    Factory mapToFactory(FactoryRequest factoryRequest);
    FactoryResponse mapToFactoryResponse(Factory factory);
    @BeanMapping(
            nullValuePropertyMappingStrategy =
                    NullValuePropertyMappingStrategy.IGNORE
    )
    void updateFactoryFromRequest(FactoryUpdateRequest factoryRequest, @MappingTarget Factory factory);
}
