package com.factory.management.modules.masterdata.dto.request;

import com.factory.management.modules.maintenance.entity.MachineOperationalStatus;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
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
public class MachineUpdateRequest {
    Long machineTypeId;
    Long teamId;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_MACHINE_CODE")
    @Size(max = 50, message = "SIZE_MACHINE_CODE")
    String code;

    @Pattern(regexp = ".*\\S.*", message = "NOT_BLANK_MACHINE_NAME")
    @Size(max = 255, message = "SIZE_MACHINE_NAME")
    String name;

    @Size(max = 500, message = "SIZE_MACHINE_DESCRIPTION")
    String description;

    @Size(max = 100, message = "SIZE_MACHINE_SERIAL_NUMBER")
    String serialNumber;

    @PastOrPresent(message = "INVALID_MACHINE_INSTALLATION_DATE")
    LocalDate installationDate;

    MachineOperationalStatus status;
    Boolean active;
}
