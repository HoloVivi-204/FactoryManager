package com.factory.management.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkScheduleRequest {
    @NotNull(message = "NOT_NULL_SCHEDULE_EMPLOYEE_ID")
    private Long employeeId;

    @NotNull(message = "NOT_NULL_SCHEDULE_SHIFT_ID")
    private Long shiftId;

    @NotNull(message = "NOT_NULL_SCHEDULE_WORK_DATE")
    private LocalDate workDate;

    @Size(max = 500, message = "SIZE_SCHEDULE_NOTE")
    private String note;

    private Boolean active;
}
