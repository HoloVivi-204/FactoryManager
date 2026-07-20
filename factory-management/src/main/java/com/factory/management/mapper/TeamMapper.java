package com.factory.management.mapper;

import com.factory.management.dto.request.TeamRequest;
import com.factory.management.dto.request.TeamUpdateRequest;
import com.factory.management.dto.response.TeamResponse;
import com.factory.management.entity.Team;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface TeamMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productionLine", ignore = true)
    @Mapping(target = "leader", ignore = true)
    Team mapToTeam(TeamRequest request);

    @Mapping(target = "productionLineId", source = "productionLine.id")
    @Mapping(target = "productionLineCode", source = "productionLine.code")
    @Mapping(target = "productionLineName", source = "productionLine.name")
    @Mapping(target = "departmentId", source = "productionLine.department.id")
    @Mapping(target = "departmentCode", source = "productionLine.department.code")
    @Mapping(target = "departmentName", source = "productionLine.department.name")
    @Mapping(target = "factoryId", source = "productionLine.department.factory.id")
    @Mapping(target = "factoryCode", source = "productionLine.department.factory.code")
    @Mapping(target = "factoryName", source = "productionLine.department.factory.name")
    @Mapping(target = "leaderEmployeeId", source = "leader.id")
    @Mapping(target = "leaderEmployeeCode", source = "leader.code")
    @Mapping(target = "leaderEmployeeName", source = "leader.fullName")
    TeamResponse mapToTeamResponse(Team team);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "productionLine", ignore = true)
    @Mapping(target = "leader", ignore = true)
    void updateTeamFromRequest(TeamUpdateRequest request, @MappingTarget Team team);
}
