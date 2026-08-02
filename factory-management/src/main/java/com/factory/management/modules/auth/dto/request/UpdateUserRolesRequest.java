package com.factory.management.modules.auth.dto.request;

import com.factory.management.modules.auth.entity.Role;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRolesRequest {
    @NotEmpty(message = "NOT_EMPTY_USER_ROLES")
    private Set<Role> roles;
}
