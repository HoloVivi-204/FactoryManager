package com.factory.management.dto.response;import com.factory.management.entity.*;import lombok.*;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor public class FinancialCategoryResponse{private Long id;private String code;private String name;private FinancialEntryType entryType;private ExpenseGroup expenseGroup;private String description;private Boolean active;}
