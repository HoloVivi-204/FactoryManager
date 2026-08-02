package com.factory.management.modules.production.dto.request;

import com.factory.management.modules.production.entity.ProductionOrderStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductionOrderStatusRequest {
    @NotNull(message = "Trạng thái lệnh sản xuất không được để trống")
    private ProductionOrderStatus status;
}
