package com.factory.management.modules.finance.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinancialVoidRequest {
    @NotBlank(message = "NOT_BLANK_FINANCIAL_VOID_REASON")
    @Size(max = 500, message = "SIZE_FINANCIAL_VOID_REASON")
    private String reason;
}
