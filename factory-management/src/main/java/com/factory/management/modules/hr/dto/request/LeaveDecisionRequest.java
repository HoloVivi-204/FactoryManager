package com.factory.management.modules.hr.dto.request;

import com.factory.management.modules.hr.entity.LeaveStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LeaveDecisionRequest {
    @NotNull(message = "NOT_NULL_LEAVE_DECISION")
    private LeaveStatus decision;

    @Size(max = 1000, message = "SIZE_REVIEW_COMMENT")
    private String comment;
}
