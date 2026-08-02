package com.factory.management.modules.inventory.dto.response;

import java.math.BigDecimal;
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
public class InventoryStockResponse {
    private Long warehouseId;
    private String warehouseCode;
    private String warehouseName;
    private Long materialId;
    private String materialCode;
    private String materialName;
    private String unit;
    private BigDecimal quantityOnHand;
    private BigDecimal averageUnitCost;
    private BigDecimal inventoryValue;
}
