package com.factory.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductionReportReviewRequest {
    @NotBlank(message = "NOT_BLANK_REVIEW_COMMENT")
    @Size(max = 1000, message = "SIZE_REVIEW_COMMENT")
    private String comment;
}
