package com.factory.management.dto.request;

import com.factory.management.entity.DataScopeType;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor public class UserDataScopeRequest {
    @NotNull private DataScopeType scopeType;
    @NotNull private Long scopeId;
}
