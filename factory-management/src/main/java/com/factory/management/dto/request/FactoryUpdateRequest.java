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
public class FactoryUpdateRequest {
    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_FACTORY_CODE")
    @Size(max = 50, message = "SIZE_FACTORY_CODE")
    String code;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_FACTORY_NAME")
    @Size(max = 255, message = "SIZE_FACTORY_NAME")
    String name;

    @Size(max = 500, message = "SIZE_FACTORY_ADDRESS")
    String address;

    Boolean active;
}
