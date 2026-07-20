package com.factory.management.entity;
import jakarta.persistence.*;import lombok.*;
@Entity @Table(name="financial_category",uniqueConstraints=@UniqueConstraint(name="uk_financial_category_code",columnNames="code"))
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor
public class FinancialCategory {@Id @GeneratedValue(strategy=GenerationType.IDENTITY)private Long id;@Column(nullable=false,length=50)private String code;@Column(nullable=false,length=255)private String name;@Enumerated(EnumType.STRING)@Column(name="entry_type",nullable=false,length=20)private FinancialEntryType entryType;@Enumerated(EnumType.STRING)@Column(name="expense_group",length=30)private ExpenseGroup expenseGroup;@Column(length=500)private String description;@Column(nullable=false)@Builder.Default private Boolean active=true;}
