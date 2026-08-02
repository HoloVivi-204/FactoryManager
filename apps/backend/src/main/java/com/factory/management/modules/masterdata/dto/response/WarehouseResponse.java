package com.factory.management.modules.masterdata.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseResponse {
    private Long id;

    private String code;

    private String name;

    private Long factoryId;

    private String factoryName;

    private String description;

    private Boolean active;
}
