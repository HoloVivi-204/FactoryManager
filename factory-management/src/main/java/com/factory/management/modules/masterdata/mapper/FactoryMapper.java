package com.factory.management.modules.masterdata.mapper;

import com.factory.management.modules.masterdata.dto.request.FactoryRequest;
import com.factory.management.modules.masterdata.dto.request.FactoryUpdateRequest;
import com.factory.management.modules.masterdata.dto.response.FactoryResponse;
import com.factory.management.modules.masterdata.entity.Factory;
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
