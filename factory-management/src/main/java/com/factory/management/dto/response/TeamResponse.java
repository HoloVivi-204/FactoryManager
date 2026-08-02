package com.factory.management.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeamResponse {
    Long id;
    String code;
    String name;
    String description;
    Boolean active;
    Long productionLineId;
    String productionLineCode;
    String productionLineName;
    Long departmentId;
    String departmentCode;
    String departmentName;
    Long factoryId;
    String factoryCode;
    String factoryName;
    Long leaderEmployeeId;
    String leaderEmployeeCode;
    String leaderEmployeeName;
}
