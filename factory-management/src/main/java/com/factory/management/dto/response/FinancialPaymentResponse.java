package com.factory.management.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class FinancialPaymentResponse {
    private Long id;
    private String paymentNo;
    private Long financialRecordId;
    private String financialRecordNo;
    private LocalDate paymentDate;
    private BigDecimal amount;
    private String referenceNo;
    private String note;
    private Long reversalOfId;
    private LocalDateTime reversedAt;
    private String reversedBy;
    private String createdBy;
    private LocalDateTime createdAt;
}
