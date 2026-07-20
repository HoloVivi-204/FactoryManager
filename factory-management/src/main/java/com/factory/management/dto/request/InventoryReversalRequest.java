package com.factory.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InventoryReversalRequest {
    @NotBlank(message = "NOT_BLANK_INVENTORY_REVERSAL_REASON")
    @Size(max = 500, message = "SIZE_INVENTORY_REVERSAL_REASON")
    private String reason;
}
