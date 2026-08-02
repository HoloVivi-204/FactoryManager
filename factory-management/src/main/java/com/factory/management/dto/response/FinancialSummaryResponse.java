package com.factory.management.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialSummaryResponse {
    private LocalDate fromDate;

    private LocalDate toDate;

    private BigDecimal totalRevenue;

    private BigDecimal totalExpense;

    private BigDecimal profit;

    private BigDecimal accountsReceivable;

    private BigDecimal accountsPayable;

    private Map<String, BigDecimal> expenseByGroup;

    private long recordCount;
}
