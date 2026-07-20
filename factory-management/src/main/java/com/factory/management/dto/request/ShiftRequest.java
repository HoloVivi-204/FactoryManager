package com.factory.management.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
public class ShiftRequest {
    @NotBlank(message = "NOT_BLANK_SHIFT_CODE")
    @Size(max = 50, message = "SIZE_SHIFT_CODE")
    String code;

    @NotBlank(message = "NOT_BLANK_SHIFT_NAME")
    @Size(max = 255, message = "SIZE_SHIFT_NAME")
    String name;

    @NotNull(message = "NOT_NULL_SHIFT_START_TIME")
    @JsonFormat(pattern = "HH:mm")
    LocalTime startTime;

    @NotNull(message = "NOT_NULL_SHIFT_END_TIME")
    @JsonFormat(pattern = "HH:mm")
    LocalTime endTime;

    Boolean active;
}
