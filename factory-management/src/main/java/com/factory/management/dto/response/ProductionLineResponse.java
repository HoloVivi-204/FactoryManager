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
public class ProductionLineResponse {
    Long id;
    String code;
    String name;
    String description;
    Boolean active;
    Long departmentId;
    String departmentCode;
    String departmentName;
    Long factoryId;
    String factoryCode;
    String factoryName;
}
