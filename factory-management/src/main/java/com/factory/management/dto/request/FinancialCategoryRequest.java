package com.factory.management.dto.request;

import com.factory.management.entity.ExpenseGroup;
import com.factory.management.entity.FinancialEntryType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinancialCategoryRequest {
    @NotBlank
    @Size(max = 50)
    private String code;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotNull
    private FinancialEntryType entryType;

    private ExpenseGroup expenseGroup;

    @Size(max = 500)
    private String description;

    private Boolean active;
}
