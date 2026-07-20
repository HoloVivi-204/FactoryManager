package com.factory.management.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;

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
