package com.factory.management.modules.production.mapper;

import com.factory.management.modules.production.dto.response.ProductionReportResponse;
import com.factory.management.modules.production.entity.ProductionReport;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProductionReportMapper {
    @Mapping(target = "sourceStagingId", source = "sourceStaging.id")
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
    @Mapping(target = "approvedById", source = "approvedBy.id")
    @Mapping(target = "approvedByCode", source = "approvedBy.code")
    @Mapping(target = "approvedByName", source = "approvedBy.fullName")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByCode", source = "createdBy.code")
    @Mapping(target = "createdByName", source = "createdBy.fullName")
    ProductionReportResponse mapToResponse(ProductionReport report);
}
