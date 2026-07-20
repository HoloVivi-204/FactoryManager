package com.factory.management.dto.response;

import com.factory.management.entity.ExpenseGroup;
import com.factory.management.entity.FinancialEntryType;
import com.factory.management.entity.FinancialRecordStatus;
import com.factory.management.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecordResponse {
    private Long id;
    private String recordNo;
    private LocalDate recordDate;
    private Long categoryId;
    private String categoryCode;
    private String categoryName;
    private FinancialEntryType entryType;
    private ExpenseGroup expenseGroup;
    private Long factoryId;
    private String factoryName;
    private Long departmentId;
    private String departmentName;
    private Long productionLineId;
    private String productionLineName;
    private BigDecimal amount;
    private BigDecimal paidAmount;
    private BigDecimal outstandingAmount;
    private LocalDate dueDate;
    private String counterparty;
    private PaymentStatus paymentStatus;
    private String referenceNo;
    private String description;
    private FinancialRecordStatus status;
    private LocalDateTime postedAt;
    private String postedBy;
    private LocalDateTime voidedAt;
    private String voidedBy;
    private String voidReason;
    private Boolean active;
    private String createdBy;
    private LocalDateTime createdAt;
    private Long version;
}
