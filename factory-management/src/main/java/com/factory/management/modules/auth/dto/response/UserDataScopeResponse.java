package com.factory.management.modules.auth.dto.response;

import com.factory.management.modules.auth.entity.DataScopeType;
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
public class UserDataScopeResponse {
    private Long id;

    private Long userId;

    private DataScopeType scopeType;

    private Long scopeId;

    private String scopeCode;

    private String scopeName;
}
