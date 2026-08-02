package com.factory.management.mapper;

import com.factory.management.dto.response.EmployeeActualStagingResponse;
import com.factory.management.entity.EmployeeActualStaging;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeActualStagingMapper {
    @Mapping(target = "productionReportStagingId", source = "productionReportStaging.id")
    @Mapping(target = "reportStatus", source = "productionReportStaging.status")
    @Mapping(target = "employeeId", source = "employee.id")
    @Mapping(target = "employeeCode", source = "employee.code")
    @Mapping(target = "employeeName", source = "employee.fullName")
    @Mapping(target = "employeeTeamId", source = "employee.team.id")
    @Mapping(target = "employeeTeamCode", source = "employee.team.code")
    @Mapping(target = "employeeTeamName", source = "employee.team.name")
    EmployeeActualStagingResponse mapToResponse(EmployeeActualStaging value);
}
