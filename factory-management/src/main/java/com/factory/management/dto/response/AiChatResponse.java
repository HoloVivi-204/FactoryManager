package com.factory.management.dto.response;

import com.factory.management.ai.AiDashboard;
import com.factory.management.ai.AiRecommendation;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class AiChatResponse {
    private String answer;
    private String dataStatus;
    private String model;
    private String workspaceRole;
    private List<String> toolsUsed;
    private List<Source> sources;
    private List<String> warnings;
    private List<AiDashboard> dashboards;
    private List<AiRecommendation> recommendations;
    private LocalDateTime answeredAt;

    @Getter
    @Builder
    public static class Source {
        private String type;
        private String label;
        private String dataStatus;
        private Integer recordCount;
        private LocalDateTime asOf;
    }
}
