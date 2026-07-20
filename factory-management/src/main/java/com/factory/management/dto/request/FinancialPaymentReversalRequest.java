package com.factory.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinancialPaymentReversalRequest {
    @NotBlank(message = "NOT_BLANK_FINANCIAL_PAYMENT_REVERSAL_REASON")
    @Size(max = 500, message = "SIZE_FINANCIAL_PAYMENT_NOTE")
    private String reason;
}
