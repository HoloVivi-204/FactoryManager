package com.factory.management.modules.ai.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiChatRequest {

    @NotBlank(message = "NOT_BLANK_AI_MESSAGE")
    @Size(max = 2000, message = "SIZE_AI_MESSAGE")
    private String message;

    /**
     * Optional role selected in the frontend workspace. The backend verifies
     * that this role is really assigned to the authenticated user and uses it
     * only to narrow permissions.
     */
    private String workspaceRole;
}
