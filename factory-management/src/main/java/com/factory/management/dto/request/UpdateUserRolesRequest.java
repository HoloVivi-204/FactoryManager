package com.factory.management.dto.request;import com.factory.management.entity.Role;import jakarta.validation.constraints.NotEmpty;import lombok.*;import java.util.Set;
@Getter @Setter @NoArgsConstructor @AllArgsConstructor public class UpdateUserRolesRequest {@NotEmpty(message="NOT_EMPTY_USER_ROLES") private Set<Role> roles;}
