package com.factory.management.dto.response;

import com.factory.management.entity.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaterialIssueStagingResponse {
    Long id;
    Long productionReportStagingId;
    ProductionReportStatus reportStatus;
    Long materialId; String materialCode; String materialName;
    MaterialIssueType issueType;
    BigDecimal quantity;
    String unit;
    String description;
    Boolean active;
}
