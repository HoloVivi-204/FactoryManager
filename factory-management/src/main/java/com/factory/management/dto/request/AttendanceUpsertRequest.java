package com.factory.management.dto.request;

import com.factory.management.entity.AttendanceSource;
import com.factory.management.entity.AttendanceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class AttendanceUpsertRequest {
    @NotNull(message = "NOT_NULL_ATTENDANCE_EMPLOYEE_ID")
    private Long employeeId;

    @NotNull(message = "NOT_NULL_ATTENDANCE_WORK_DATE")
    private LocalDate workDate;

    private LocalDateTime checkIn;
    private LocalDateTime checkOut;

    @NotNull(message = "NOT_NULL_ATTENDANCE_STATUS")
    private AttendanceStatus attendanceStatus;

    private AttendanceSource source;

    @Size(max = 1000, message = "SIZE_ATTENDANCE_NOTE")
    private String note;
}
