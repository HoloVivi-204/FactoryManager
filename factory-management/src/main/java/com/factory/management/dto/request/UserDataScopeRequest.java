package com.factory.management.dto.request;
import com.factory.management.entity.DataScopeType;
import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor public class UserDataScopeRequest {
    @NotNull private DataScopeType scopeType;
    @NotNull private Long scopeId;
}
