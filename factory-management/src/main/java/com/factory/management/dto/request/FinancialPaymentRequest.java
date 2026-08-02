package com.factory.management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FinancialPaymentRequest {
    @NotNull(message = "NOT_NULL_FINANCIAL_PAYMENT_DATE")
    private LocalDate paymentDate;

    @NotNull(message = "NOT_NULL_FINANCIAL_PAYMENT_AMOUNT")
    @DecimalMin(value = "0.01", message = "INVALID_FINANCIAL_PAYMENT_AMOUNT")
    private BigDecimal amount;

    @Size(max = 100, message = "SIZE_FINANCIAL_PAYMENT_REFERENCE")
    private String referenceNo;

    @Size(max = 500, message = "SIZE_FINANCIAL_PAYMENT_NOTE")
    private String note;
}
