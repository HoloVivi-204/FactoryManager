package com.factory.management.dto.response;

import com.factory.management.entity.DepartmentType;
import lombok.*;
import lombok.experimental.FieldDefaults;

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
