package com.factory.management.dto.response;
import com.factory.management.entity.DataScopeType;
import lombok.*;
@Getter @Setter @Builder @NoArgsConstructor @AllArgsConstructor public class UserDataScopeResponse {
    private Long id; private Long userId; private DataScopeType scopeType; private Long scopeId; private String scopeCode; private String scopeName;
}
