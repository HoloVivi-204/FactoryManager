package com.factory.management.modules.masterdata.dto.response;

import com.factory.management.modules.masterdata.entity.DowntimeReasonType;
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
public class DowntimeReasonResponse {
    Long id;
    String code;
    String name;
    String description;
    DowntimeReasonType reasonType;
    Boolean active;
}
