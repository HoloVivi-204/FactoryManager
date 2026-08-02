package com.factory.management.modules.ai.dto.response;

import com.factory.management.modules.ai.AiDashboard;
import com.factory.management.modules.ai.AiRecommendation;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

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
