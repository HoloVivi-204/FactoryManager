package com.factory.management.modules.hr.dto.request;

import com.factory.management.modules.hr.entity.NotificationSeverity;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotificationCreateRequest {
    @NotNull(message = "NOT_NULL_NOTIFICATION_RECIPIENT")
    private Long employeeId;

    @NotBlank(message = "NOT_BLANK_NOTIFICATION_TITLE")
    @Size(max = 255, message = "SIZE_NOTIFICATION_TITLE")
    private String title;

    @NotBlank(message = "NOT_BLANK_NOTIFICATION_MESSAGE")
    @Size(max = 2000, message = "SIZE_NOTIFICATION_MESSAGE")
    private String message;

    private NotificationSeverity severity;

    @Size(max = 500, message = "SIZE_NOTIFICATION_ACTION_URL")
    private String actionUrl;
}
