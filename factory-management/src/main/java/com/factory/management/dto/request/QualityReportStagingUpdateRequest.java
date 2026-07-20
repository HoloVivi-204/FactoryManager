package com.factory.management.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QualityReportStagingUpdateRequest {
    Long productionReportStagingId;
    Long qualityErrorTypeId;
    @Positive(message = "INVALID_QUALITY_QUANTITY") Long quantity;
    @Size(max = 1000, message = "SIZE_QUALITY_DESCRIPTION") String description;
    Boolean active;
}
