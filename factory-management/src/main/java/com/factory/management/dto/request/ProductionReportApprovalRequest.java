package com.factory.management.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProductionReportApprovalRequest {
    @Size(max = 1000, message = "SIZE_PRODUCTION_REPORT_REMARK") String remark;
}
