package com.factory.management.dto.request;

import com.factory.management.entity.MaterialIssueType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaterialIssueStagingUpdateRequest {
    Long productionReportStagingId;
    Long materialId;
    MaterialIssueType issueType;
    @DecimalMin(value = "0.0", inclusive = false, message = "INVALID_MATERIAL_ISSUE_QUANTITY") BigDecimal quantity;
    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_MATERIAL_ISSUE_UNIT")
    @Size(max = 50, message = "SIZE_MATERIAL_ISSUE_UNIT") String unit;
    @Size(max = 1000, message = "SIZE_MATERIAL_ISSUE_DESCRIPTION") String description;
    Boolean active;
}
