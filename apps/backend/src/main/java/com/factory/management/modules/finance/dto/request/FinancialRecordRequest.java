package com.factory.management.modules.finance.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinancialRecordRequest {
    @NotNull
    private LocalDate recordDate;

    @NotNull
    private Long categoryId;

    @NotNull
    private Long factoryId;

    private Long departmentId;

    private Long productionLineId;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    @DecimalMin("0.00")
    private BigDecimal paidAmount;

    private LocalDate dueDate;

    @Size(max = 255)
    private String counterparty;

    @Size(max = 100)
    private String referenceNo;

    @Size(max = 1000)
    private String description;

    private Boolean active;
}
