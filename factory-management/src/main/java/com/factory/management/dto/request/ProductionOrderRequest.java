package com.factory.management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ProductionOrderRequest {

    @NotNull(message = "Kế hoạch sản xuất không được để trống")
    private Long productionPlanId;

    @NotNull(message = "Tổ sản xuất không được để trống")
    private Long teamId;

    @NotNull(message = "Máy sản xuất không được để trống")
    private Long machineId;

    @NotNull(message = "Ca làm việc không được để trống")
    private Long shiftId;

    @NotNull(message = "Ngày bắt đầu lệnh không được để trống")
    private LocalDate scheduledStart;

    @NotNull(message = "Ngày kết thúc lệnh không được để trống")
    private LocalDate scheduledEnd;

    @NotNull(message = "Sản lượng lệnh không được để trống")
    @DecimalMin(value = "0.001", message = "Sản lượng lệnh phải lớn hơn 0")
    @Digits(integer = 16, fraction = 3, message = "Sản lượng có tối đa 16 số nguyên và 3 số thập phân")
    private BigDecimal plannedQuantity;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String note;
}
