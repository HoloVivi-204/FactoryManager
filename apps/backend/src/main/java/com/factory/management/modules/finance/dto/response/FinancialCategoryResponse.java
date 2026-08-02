package com.factory.management.modules.finance.dto.response;

import com.factory.management.modules.finance.entity.ExpenseGroup;
import com.factory.management.modules.finance.entity.FinancialEntryType;
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
public class FinancialCategoryResponse {
    private Long id;

    private String code;

    private String name;

    private FinancialEntryType entryType;

    private ExpenseGroup expenseGroup;

    private String description;

    private Boolean active;
}
