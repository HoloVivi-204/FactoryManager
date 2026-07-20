package com.factory.management.dto.response;

import com.factory.management.entity.QualityErrorSeverity;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QualityErrorTypeResponse {
    Long id;
    String code;
    String name;
    String description;
    QualityErrorSeverity severity;
    Boolean active;
}
