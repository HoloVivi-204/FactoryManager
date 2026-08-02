package com.factory.management.modules.production.dto.request;

import com.factory.management.modules.auth.entity.DataScopeType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
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
public class DailyCloseBatchRequest {
    @NotNull(message = "Ngày chốt không được để trống")
    private LocalDate reportDate;

    @NotNull(message = "Loại phạm vi chốt không được để trống")
    private DataScopeType scopeType;

    @NotNull(message = "ID phạm vi chốt không được để trống")
    private Long scopeId;

    private Long shiftId;
}
