package com.factory.management.dto.response;

import com.factory.management.entity.InventoryTransactionType;
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
public class InventoryTransactionResponse {
    private Long id;
    private String transactionNo;
    private LocalDate transactionDate;
    private InventoryTransactionType transactionType;
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String unit;
    private BigDecimal quantity;
    private BigDecimal unitCost;
    private BigDecimal totalAmount;
    private String referenceNo;
    private String description;
    private Long reversalOfId;
    private LocalDateTime reversedAt;
    private String reversedBy;
    private Boolean active;
    private String createdBy;
    private LocalDateTime createdAt;
}
