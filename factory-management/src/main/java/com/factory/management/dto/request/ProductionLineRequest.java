package com.factory.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductionLineRequest {

    @NotNull(message = "NOT_NULL_PRODUCTION_LINE_DEPARTMENT_ID")
    Long departmentId;

    @NotBlank(message = "NOT_BLANK_PRODUCTION_LINE_CODE")
    @Size(max = 50, message = "SIZE_PRODUCTION_LINE_CODE")
    String code;

    @NotBlank(message = "NOT_BLANK_PRODUCTION_LINE_NAME")
    @Size(max = 255, message = "SIZE_PRODUCTION_LINE_NAME")
    String name;

    @Size(max = 500, message = "SIZE_PRODUCTION_LINE_DESCRIPTION")
    String description;

    Boolean active;
}
