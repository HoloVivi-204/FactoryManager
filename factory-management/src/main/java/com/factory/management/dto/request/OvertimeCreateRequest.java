package com.factory.management.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OvertimeCreateRequest {
    private Long employeeId;

    @NotNull(message = "NOT_NULL_OVERTIME_WORK_DATE")
    private LocalDate workDate;

    @NotNull(message = "NOT_NULL_OVERTIME_MINUTES")
    @Min(value = 1, message = "INVALID_OVERTIME_MINUTES")
    @Max(value = 720, message = "INVALID_OVERTIME_MINUTES")
    private Integer requestedMinutes;

    @NotBlank(message = "NOT_BLANK_OVERTIME_REASON")
    @Size(max = 1000, message = "SIZE_OVERTIME_REASON")
    private String reason;
}
