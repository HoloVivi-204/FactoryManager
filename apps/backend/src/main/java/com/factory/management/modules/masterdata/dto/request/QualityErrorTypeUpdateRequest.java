package com.factory.management.modules.masterdata.dto.request;

import com.factory.management.modules.masterdata.entity.QualityErrorSeverity;
import jakarta.validation.constraints.Pattern;
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
public class QualityErrorTypeUpdateRequest {
    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_QUALITY_ERROR_TYPE_CODE")
    @Size(max = 50, message = "SIZE_QUALITY_ERROR_TYPE_CODE")
    String code;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_QUALITY_ERROR_TYPE_NAME")
    @Size(max = 255, message = "SIZE_QUALITY_ERROR_TYPE_NAME")
    String name;

    @Size(max = 500, message = "SIZE_QUALITY_ERROR_TYPE_DESCRIPTION")
    String description;

    QualityErrorSeverity severity;
    Boolean active;
}
