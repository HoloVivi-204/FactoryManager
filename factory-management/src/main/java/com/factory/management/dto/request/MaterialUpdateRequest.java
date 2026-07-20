package com.factory.management.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaterialUpdateRequest {
    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_MATERIAL_CODE")
    @Size(max = 50, message = "SIZE_MATERIAL_CODE")
    String code;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_MATERIAL_NAME")
    @Size(max = 255, message = "SIZE_MATERIAL_NAME")
    String name;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_MATERIAL_UNIT")
    @Size(max = 50, message = "SIZE_MATERIAL_UNIT")
    String unit;

    @Size(max = 500, message = "SIZE_MATERIAL_DESCRIPTION")
    String description;

    Boolean active;
}
