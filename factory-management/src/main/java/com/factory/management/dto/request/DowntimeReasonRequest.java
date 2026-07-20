package com.factory.management.dto.request;

import com.factory.management.entity.DowntimeReasonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DowntimeReasonRequest {
    @NotBlank(message = "NOT_BLANK_DOWNTIME_REASON_CODE")
    @Size(max = 50, message = "SIZE_DOWNTIME_REASON_CODE")
    String code;

    @NotBlank(message = "NOT_BLANK_DOWNTIME_REASON_NAME")
    @Size(max = 255, message = "SIZE_DOWNTIME_REASON_NAME")
    String name;

    @Size(max = 500, message = "SIZE_DOWNTIME_REASON_DESCRIPTION")
    String description;

    @NotNull(message = "NOT_NULL_DOWNTIME_REASON_TYPE")
    DowntimeReasonType reasonType;

    Boolean active;
}
