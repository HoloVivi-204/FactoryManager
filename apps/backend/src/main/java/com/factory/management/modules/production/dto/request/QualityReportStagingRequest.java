package com.factory.management.modules.production.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
