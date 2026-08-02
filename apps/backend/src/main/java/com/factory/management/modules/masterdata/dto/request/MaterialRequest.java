package com.factory.management.modules.masterdata.dto.request;

import jakarta.validation.constraints.NotBlank;
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
public class MaterialRequest {
    @NotBlank(message = "NOT_BLANK_MATERIAL_CODE")
    @Size(max = 50, message = "SIZE_MATERIAL_CODE")
    String code;

    @NotBlank(message = "NOT_BLANK_MATERIAL_NAME")
    @Size(max = 255, message = "SIZE_MATERIAL_NAME")
    String name;

    @NotBlank(message = "NOT_BLANK_MATERIAL_UNIT")
    @Size(max = 50, message = "SIZE_MATERIAL_UNIT")
    String unit;

    @Size(max = 500, message = "SIZE_MATERIAL_DESCRIPTION")
    String description;

    Boolean active;
}
