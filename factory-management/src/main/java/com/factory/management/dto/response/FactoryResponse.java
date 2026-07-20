package com.factory.management.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class FactoryResponse {
    private Long id;
    private String code;
    private String name;
    private String address;
    private Boolean active;
}
