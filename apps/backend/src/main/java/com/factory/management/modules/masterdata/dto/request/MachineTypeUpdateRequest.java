package com.factory.management.modules.masterdata.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.experimental.FieldDefaults;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MachineTypeUpdateRequest {
    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_MACHINE_TYPE_CODE")
    @Size(max = 50, message = "SIZE_MACHINE_TYPE_CODE")
    String code;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_MACHINE_TYPE_NAME")
    @Size(max = 255, message = "SIZE_MACHINE_TYPE_NAME")
    String name;

    @Size(max = 500, message = "SIZE_MACHINE_TYPE_DESCRIPTION")
    String description;

    Boolean active;
}
