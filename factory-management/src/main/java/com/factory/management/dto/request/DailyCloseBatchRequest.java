package com.factory.management.dto.request;

import com.factory.management.entity.DataScopeType;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DailyCloseBatchRequest {
    @NotNull(message = "Ngày chốt không được để trống")
    private LocalDate reportDate;

    @NotNull(message = "Loại phạm vi chốt không được để trống")
    private DataScopeType scopeType;

    @NotNull(message = "ID phạm vi chốt không được để trống")
    private Long scopeId;

    private Long shiftId;
}
