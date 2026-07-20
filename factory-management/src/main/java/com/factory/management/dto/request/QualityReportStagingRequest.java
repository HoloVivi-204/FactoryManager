package com.factory.management.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QualityReportStagingRequest {
    @NotNull(message = "NOT_NULL_QUALITY_REPORT_ID") Long productionReportStagingId;
    @NotNull(message = "NOT_NULL_QUALITY_ERROR_TYPE_ID") Long qualityErrorTypeId;
    @NotNull(message = "NOT_NULL_QUALITY_QUANTITY")
    @Positive(message = "INVALID_QUALITY_QUANTITY") Long quantity;
    @Size(max = 1000, message = "SIZE_QUALITY_DESCRIPTION") String description;
    Boolean active;
}
