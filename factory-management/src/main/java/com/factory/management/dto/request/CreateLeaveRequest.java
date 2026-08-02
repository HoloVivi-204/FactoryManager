package com.factory.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeaveRequest {
    @NotNull(message = "NOT_NULL_LEAVE_FROM_DATE")
    private LocalDate fromDate;

    @NotNull(message = "NOT_NULL_LEAVE_TO_DATE")
    private LocalDate toDate;

    @NotBlank(message = "NOT_BLANK_LEAVE_TYPE")
    @Size(max = 50)
    private String leaveType;

    @NotBlank(message = "NOT_BLANK_LEAVE_REASON")
    @Size(max = 1000)
    private String reason;
}
