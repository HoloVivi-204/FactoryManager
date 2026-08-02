package com.factory.management.dto.response;

import com.factory.management.entity.DepartmentType;
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
public class DepartmentResponse {
    Long id;
    String code;
    String name;
    String description;
    DepartmentType departmentType;
    Boolean active;
    Long factoryId;
    String factoryCode;
    String factoryName;
}
