package com.factory.management.modules.ai.controller;

import com.factory.management.modules.ai.AiChatService;
import com.factory.management.modules.ai.dto.request.AiChatRequest;
import com.factory.management.modules.ai.dto.response.AiChatResponse;
import com.factory.management.common.api.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("${api.prefix}/ai-chat")
@RequiredArgsConstructor
public class AiChatController {
    private final AiChatService service;

    @PostMapping("/messages")
    @PreAuthorize("isAuthenticated()")
    public ApiResponse<AiChatResponse> ask(@Valid @RequestBody AiChatRequest request) {
        return ApiResponse.<AiChatResponse>builder()
                .message("Chatbot đã trả lời")
                .result(service.ask(request))
                .build();
    }
}
