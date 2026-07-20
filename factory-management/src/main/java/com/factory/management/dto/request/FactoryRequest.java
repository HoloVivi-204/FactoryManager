package com.factory.management.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FactoryRequest{
    @NotBlank(message = "NOT_BLANK_FACTORY_CODE")
    @Size(max = 50, message = "SIZE_FACTORY_CODE")
    private String code;

    @NotBlank(message = "NOT_BLANK_FACTORY_NAME")
    @Size(max = 255, message = "SIZE_FACTORY_NAME")
    private String name;

    @NotBlank(message = "NOT_BLANK_FACTORY_ADDRESS")
    @Size(max = 500, message = "SIZE_FACTORY_ADDRESS")
    private String address;

    private Boolean active;
}
