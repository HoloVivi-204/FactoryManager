package com.factory.management.modules.masterdata.dto.request;

import com.factory.management.modules.masterdata.entity.DepartmentType;
import jakarta.validation.constraints.Size;
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
public class DepartmentUpdateRequest {
    Long factoryId;

    DepartmentType departmentType;

    @Size(max = 500, message = "SIZE_DEPARTMENT_DESCRIPTION")
    String description;

    Boolean active;
}
