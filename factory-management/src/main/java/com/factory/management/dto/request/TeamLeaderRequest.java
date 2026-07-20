package com.factory.management.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeamLeaderRequest {
    @NotNull(message = "NOT_NULL_TEAM_LEADER_EMPLOYEE_ID")
    Long employeeId;
}
