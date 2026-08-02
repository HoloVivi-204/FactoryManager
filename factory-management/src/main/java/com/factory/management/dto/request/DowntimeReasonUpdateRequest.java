package com.factory.management.dto.request;

import com.factory.management.entity.DowntimeReasonType;
import jakarta.validation.constraints.Pattern;
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
public class DowntimeReasonUpdateRequest {
    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_DOWNTIME_REASON_CODE")
    @Size(max = 50, message = "SIZE_DOWNTIME_REASON_CODE")
    String code;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_DOWNTIME_REASON_NAME")
    @Size(max = 255, message = "SIZE_DOWNTIME_REASON_NAME")
    String name;

    @Size(max = 500, message = "SIZE_DOWNTIME_REASON_DESCRIPTION")
    String description;

    DowntimeReasonType reasonType;

    Boolean active;
}
