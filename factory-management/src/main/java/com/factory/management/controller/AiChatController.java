package com.factory.management.controller;

import com.factory.management.ai.AiChatService;
import com.factory.management.dto.request.AiChatRequest;
import com.factory.management.dto.response.AiChatResponse;
import com.factory.management.dto.response.ApiResponse;
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
