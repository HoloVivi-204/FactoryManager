package com.factory.management.modules.masterdata.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TeamRequest {

    @NotNull(message = "NOT_NULL_TEAM_PRODUCTION_LINE_ID")
    Long productionLineId;

    @NotBlank(message = "NOT_BLANK_TEAM_CODE")
    @Size(max = 50, message = "SIZE_TEAM_CODE")
    String code;

    @NotBlank(message = "NOT_BLANK_TEAM_NAME")
    @Size(max = 255, message = "SIZE_TEAM_NAME")
    String name;

    @Size(max = 500, message = "SIZE_TEAM_DESCRIPTION")
    String description;

    Boolean active;
}
