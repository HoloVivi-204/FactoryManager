package com.factory.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductionReportReviewRequest {
    @NotBlank(message = "NOT_BLANK_REVIEW_COMMENT")
    @Size(max = 1000, message = "SIZE_REVIEW_COMMENT")
    private String comment;
}
