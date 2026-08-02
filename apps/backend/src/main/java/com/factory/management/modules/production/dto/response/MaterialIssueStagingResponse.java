package com.factory.management.modules.production.dto.response;

import com.factory.management.modules.production.entity.MaterialIssueType;
import com.factory.management.modules.production.entity.ProductionReportStatus;
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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
