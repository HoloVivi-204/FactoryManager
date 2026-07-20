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
public class DepartmentTypeResponse {
    DepartmentType type;
    String codeSuffix;
    String name;
    String description;
}
