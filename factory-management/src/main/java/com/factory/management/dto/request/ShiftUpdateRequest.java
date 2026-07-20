package com.factory.management.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ShiftUpdateRequest {
    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_SHIFT_CODE")
    @Size(max = 50, message = "SIZE_SHIFT_CODE")
    String code;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_SHIFT_NAME")
    @Size(max = 255, message = "SIZE_SHIFT_NAME")
    String name;

    @JsonFormat(pattern = "HH:mm")
    LocalTime startTime;

    @JsonFormat(pattern = "HH:mm")
    LocalTime endTime;

    Boolean active;
}
