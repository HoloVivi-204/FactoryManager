package com.factory.management.dto.request;

import com.factory.management.entity.InventoryTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class InventoryTransactionRequest {
    @NotNull
    private LocalDate transactionDate;

    @NotNull
    private InventoryTransactionType transactionType;

    @NotNull
    private Long warehouseId;

    @NotNull
    private Long materialId;

    @NotNull
    @DecimalMin("0.001")
    private BigDecimal quantity;

    @DecimalMin("0.00")
    private BigDecimal unitCost;

    @Size(max = 100)
    private String referenceNo;

    @Size(max = 1000)
    private String description;
}
