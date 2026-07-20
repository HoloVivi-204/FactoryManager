package com.factory.management.dto.response;

import com.factory.management.entity.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QualityReportStagingResponse {
    Long id;
    Long productionReportStagingId;
    ProductionReportStatus reportStatus;
    Long qualityErrorTypeId;
    String qualityErrorTypeCode;
    String qualityErrorTypeName;
    QualityErrorSeverity severity;
    Long quantity;
    String description;
    Boolean active;
}
