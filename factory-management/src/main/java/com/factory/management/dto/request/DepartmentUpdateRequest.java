package com.factory.management.dto.request;

import com.factory.management.entity.DepartmentType;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepartmentUpdateRequest {
    Long factoryId;

    DepartmentType departmentType;

    @Size(max = 500, message = "SIZE_DEPARTMENT_DESCRIPTION")
    String description;

    Boolean active;
}
