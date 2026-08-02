package com.factory.management.modules.masterdata.mapper;

import com.factory.management.modules.masterdata.dto.response.DepartmentResponse;
import com.factory.management.modules.masterdata.entity.Department;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepartmentMapper {

    @Mapping(target = "factoryId", source = "factory.id")
    @Mapping(target = "factoryCode", source = "factory.code")
    @Mapping(target = "factoryName", source = "factory.name")
    DepartmentResponse mapToDepartmentResponse(Department department);

}
