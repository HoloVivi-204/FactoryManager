package com.factory.management.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeamUpdateRequest {

    Long productionLineId;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_TEAM_CODE")
    @Size(max = 50, message = "SIZE_TEAM_CODE")
    String code;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_TEAM_NAME")
    @Size(max = 255, message = "SIZE_TEAM_NAME")
    String name;

    @Size(max = 500, message = "SIZE_TEAM_DESCRIPTION")
    String description;

    Boolean active;
}
