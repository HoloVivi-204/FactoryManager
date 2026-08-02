package com.factory.management.dto.response;

import com.factory.management.entity.ProductionReportStatus;
import com.factory.management.entity.QualityErrorSeverity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
