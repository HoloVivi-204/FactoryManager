package com.factory.management.modules.auth.dto.request;

import com.factory.management.modules.auth.entity.DataScopeType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor public class UserDataScopeRequest {
    @NotNull private DataScopeType scopeType;
    @NotNull private Long scopeId;
}
