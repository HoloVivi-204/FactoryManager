package com.factory.management.mapper;

import com.factory.management.dto.response.ProductionReportStagingResponse;
import com.factory.management.entity.ProductionReportStaging;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductionReportStagingMapper {
    @Mapping(target = "shiftId", source = "shift.id")
    @Mapping(target = "shiftCode", source = "shift.code")
    @Mapping(target = "shiftName", source = "shift.name")
    @Mapping(target = "factoryId", source = "factory.id")
    @Mapping(target = "factoryCode", source = "factory.code")
    @Mapping(target = "factoryName", source = "factory.name")
    @Mapping(target = "departmentId", source = "department.id")
    @Mapping(target = "departmentCode", source = "department.code")
    @Mapping(target = "departmentName", source = "department.name")
    @Mapping(target = "productionLineId", source = "productionLine.id")
    @Mapping(target = "productionLineCode", source = "productionLine.code")
    @Mapping(target = "productionLineName", source = "productionLine.name")
    @Mapping(target = "teamId", source = "team.id")
    @Mapping(target = "teamCode", source = "team.code")
    @Mapping(target = "teamName", source = "team.name")
    @Mapping(target = "leaderEmployeeId", source = "leaderEmployee.id")
    @Mapping(target = "leaderEmployeeCode", source = "leaderEmployee.code")
    @Mapping(target = "leaderEmployeeName", source = "leaderEmployee.fullName")
    @Mapping(target = "machineId", source = "machine.id")
    @Mapping(target = "machineCode", source = "machine.code")
    @Mapping(target = "machineName", source = "machine.name")
    ProductionReportStagingResponse mapToResponse(ProductionReportStaging report);
}
