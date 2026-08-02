package com.factory.management.modules.hr.dto.request;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmployeeKpiRequest {
    @NotNull(message = "NOT_NULL_KPI_EMPLOYEE_ID")
    private Long employeeId;

    @NotNull(message = "NOT_NULL_KPI_PERIOD_START")
    private LocalDate periodStart;

    @NotNull(message = "NOT_NULL_KPI_PERIOD_END")
    private LocalDate periodEnd;

    @DecimalMin(value = "0", message = "INVALID_KPI_SCORE")
    @DecimalMax(value = "100", message = "INVALID_KPI_SCORE")
    private BigDecimal score;

    @DecimalMin(value = "0", message = "INVALID_KPI_SCORE")
    @DecimalMax(value = "100", message = "INVALID_KPI_SCORE")
    private BigDecimal productivityScore;

    @DecimalMin(value = "0", message = "INVALID_KPI_SCORE")
    @DecimalMax(value = "100", message = "INVALID_KPI_SCORE")
    private BigDecimal qualityScore;

    @DecimalMin(value = "0", message = "INVALID_KPI_SCORE")
    @DecimalMax(value = "100", message = "INVALID_KPI_SCORE")
    private BigDecimal attendanceScore;

    @Size(max = 1000, message = "SIZE_KPI_NOTE")
    private String note;
}
