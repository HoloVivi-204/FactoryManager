package com.factory.management.mapper;

import com.factory.management.dto.request.EmployeeRequest;
import com.factory.management.dto.request.EmployeeUpdateRequest;
import com.factory.management.dto.response.EmployeeResponse;
import com.factory.management.entity.Employee;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    Employee mapToEmployee(EmployeeRequest request);

    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamCode", source = "team.code")
    @Mapping(target = "teamName", source = "team.name")
    @Mapping(target = "productionLineId", source = "team.productionLine.id")
    @Mapping(target = "productionLineCode", source = "team.productionLine.code")
    @Mapping(target = "departmentId", source = "team.productionLine.department.id")
    @Mapping(target = "departmentCode", source = "team.productionLine.department.code")
    @Mapping(target = "factoryId", source = "team.productionLine.department.factory.id")
    @Mapping(target = "factoryCode", source = "team.productionLine.department.factory.code")
    EmployeeResponse mapToEmployeeResponse(Employee employee);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "team", ignore = true)
    void updateEmployeeFromRequest(EmployeeUpdateRequest request, @MappingTarget Employee employee);
}
