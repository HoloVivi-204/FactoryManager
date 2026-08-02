package com.factory.management.modules.ai;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "ai")
public class AiProperties {
    /** AI is opt-in even though the default provider runs locally. */
    private boolean enabled = false;
    private String chatUrl = "http://localhost:11434/api/chat";
    private String model = "qwen3:4b";
    private int maxToolCalls = 3;
    private int maxOutputTokens = 1200;
    private int connectTimeoutSeconds = 5;
    private int readTimeoutSeconds = 45;
    private int requestsPerMinute = 10;
}
