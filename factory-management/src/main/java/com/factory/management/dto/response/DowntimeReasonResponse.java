package com.factory.management.dto.response;

import com.factory.management.entity.DowntimeReasonType;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DowntimeReasonResponse {
    Long id;
    String code;
    String name;
    String description;
    DowntimeReasonType reasonType;
    Boolean active;
}
