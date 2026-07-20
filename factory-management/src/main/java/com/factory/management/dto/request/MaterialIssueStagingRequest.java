package com.factory.management.dto.request;

import com.factory.management.entity.MaterialIssueType;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MaterialIssueStagingRequest {
    @NotNull(message = "NOT_NULL_MATERIAL_ISSUE_REPORT_ID") Long productionReportStagingId;
    @NotNull(message = "NOT_NULL_MATERIAL_ISSUE_MATERIAL_ID") Long materialId;
    @NotNull(message = "NOT_NULL_MATERIAL_ISSUE_TYPE") MaterialIssueType issueType;
    @NotNull(message = "NOT_NULL_MATERIAL_ISSUE_QUANTITY")
    @DecimalMin(value = "0.0", inclusive = false, message = "INVALID_MATERIAL_ISSUE_QUANTITY") BigDecimal quantity;
    @NotBlank(message = "NOT_BLANK_MATERIAL_ISSUE_UNIT")
    @Size(max = 50, message = "SIZE_MATERIAL_ISSUE_UNIT") String unit;
    @Size(max = 1000, message = "SIZE_MATERIAL_ISSUE_DESCRIPTION") String description;
    Boolean active;
}
