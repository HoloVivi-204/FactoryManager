package com.factory.management.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductRequest {

    @NotBlank(message = "Mã sản phẩm không được để trống")
    @Size(max = 50, message = "Mã sản phẩm không được vượt quá 50 ký tự")
    private String code;

    @NotBlank(message = "Tên sản phẩm không được để trống")
    @Size(max = 255, message = "Tên sản phẩm không được vượt quá 255 ký tự")
    private String name;

    @NotBlank(message = "Đơn vị tính không được để trống")
    @Size(max = 50, message = "Đơn vị tính không được vượt quá 50 ký tự")
    private String unit;

    @NotNull(message = "Chu kỳ tiêu chuẩn không được để trống")
    @DecimalMin(value = "0.001", message = "Chu kỳ tiêu chuẩn phải lớn hơn 0")
    @Digits(integer = 9, fraction = 3, message = "Chu kỳ tiêu chuẩn có tối đa 9 số nguyên và 3 số thập phân")
    private BigDecimal standardCycleSeconds;

    private Boolean active;
}
