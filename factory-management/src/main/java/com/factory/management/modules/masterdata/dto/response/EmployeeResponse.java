package com.factory.management.modules.masterdata.dto.response;

import java.time.LocalDate;
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
public class EmployeeResponse {
    Long id;
    String code;
    String fullName;
    String position;
    LocalDate hireDate;
    Boolean active;
    Long teamId;
    String teamCode;
    String teamName;
    Long productionLineId;
    String productionLineCode;
    Long departmentId;
    String departmentCode;
    Long factoryId;
    String factoryCode;
}
