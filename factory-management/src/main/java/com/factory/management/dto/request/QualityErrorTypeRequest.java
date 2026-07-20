package com.factory.management.dto.request;

import com.factory.management.entity.QualityErrorSeverity;
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
public class QualityErrorTypeRequest {
    @NotBlank(message = "NOT_BLANK_QUALITY_ERROR_TYPE_CODE")
    @Size(max = 50, message = "SIZE_QUALITY_ERROR_TYPE_CODE")
    String code;

    @NotBlank(message = "NOT_BLANK_QUALITY_ERROR_TYPE_NAME")
    @Size(max = 255, message = "SIZE_QUALITY_ERROR_TYPE_NAME")
    String name;

    @Size(max = 500, message = "SIZE_QUALITY_ERROR_TYPE_DESCRIPTION")
    String description;

    @NotNull(message = "NOT_NULL_QUALITY_ERROR_TYPE_SEVERITY")
    QualityErrorSeverity severity;

    Boolean active;
}
