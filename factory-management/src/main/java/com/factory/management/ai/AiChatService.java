package com.factory.management.ai;

import com.factory.management.dto.request.AiChatRequest;
import com.factory.management.dto.response.AiChatResponse;
import com.factory.management.service.impl.AuditService;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AiChatService {
    private final AiScopeContextService contextService;
    private final AiToolRegistry toolRegistry;
    private final OllamaChatClient modelClient;
    private final AiVisualizationService visualizationService;
    private final AiRecommendationService recommendationService;
    private final AuditService auditService;
    private final AiRateLimitService rateLimitService;

    public AiChatResponse ask(AiChatRequest request) {
        AiToolContext context = contextService.current(request.getWorkspaceRole());
        rateLimitService.check(context.userId());
        var definitions = toolRegistry.definitions(context);
        String message = request.getMessage().trim();
        AiModelTurn turn = modelClient.plan(message, context, definitions);

        List<AiToolResult> results = new ArrayList<>();
        for (AiModelTurn.ToolCall call : turn.toolCalls()) {
            results.add(toolRegistry.execute(call.name(), call.arguments(), context));
        }
        AiModelCompletion completion = results.isEmpty()
                ? new AiModelCompletion(turn.directAnswer(), AiVisualizationSpec.empty())
                : modelClient.complete(message, context, turn, results, definitions);
        String answer = completion.answer();

        List<String> toolsUsed = results.stream().map(AiToolResult::toolName).distinct().toList();
        List<AiChatResponse.Source> sources = results.stream().flatMap(result -> result.sources().stream())
                .map(source -> AiChatResponse.Source.builder()
                        .type(source.type())
                        .label(source.label())
                        .dataStatus(source.dataStatus())
                        .recordCount(source.recordCount())
                        .asOf(source.asOf())
                        .build())
                .toList();
        List<String> warnings = new ArrayList<>(results.stream()
                .flatMap(result -> result.warnings().stream())
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new)));
        List<AiDashboard> dashboards = visualizationService.materialize(
                completion.visualization(), results);
        List<AiRecommendation> recommendations = recommendationService.recommend(message, results);
        String dataStatus = resolveDataStatus(results);

        auditService.record("AI_QUERY", "AI_CHAT", null, auditDetails(
                context, toolsUsed, dataStatus, sources.stream().mapToInt(AiChatResponse.Source::getRecordCount).sum()));
        return AiChatResponse.builder()
                .answer(answer)
                .dataStatus(dataStatus)
                .model(turn.model())
                .workspaceRole(context.workspaceRole() == null ? null : context.workspaceRole().name())
                .toolsUsed(toolsUsed)
                .sources(sources)
                .warnings(warnings)
                .dashboards(dashboards)
                .recommendations(recommendations)
                .answeredAt(LocalDateTime.now())
                .build();
    }

    private String resolveDataStatus(List<AiToolResult> results) {
        if (results.isEmpty()) {
            return "NO_BUSINESS_DATA";
        }
        if (results.stream().allMatch(result -> "NO_BUSINESS_DATA".equals(result.dataStatus()))) {
            return "NO_BUSINESS_DATA";
        }
        boolean temporary = results.stream().anyMatch(result ->
                result.dataStatus().contains("TEMPORARY"));
        boolean official = results.stream().anyMatch(result ->
                result.dataStatus().contains("OFFICIAL"));
        if (temporary && official) {
            return "OFFICIAL_WITH_TEMPORARY";
        }
        if (temporary) {
            return "TEMPORARY_UNCONFIRMED";
        }
        return "OFFICIAL";
    }

    private String auditDetails(
            AiToolContext context,
            List<String> tools,
            String dataStatus,
            int recordCount
    ) {
        return toSafeJson(
                context.workspaceRole() == null ? null : context.workspaceRole().name(),
                tools,
                dataStatus,
                recordCount,
                context.accessibleTeamIds().size());
    }

    private String toSafeJson(
            String workspaceRole,
            List<String> tools,
            String dataStatus,
            int recordCount,
            int accessibleTeamCount
    ) {
        String role = workspaceRole == null ? "null" : quote(workspaceRole);
        String toolJson = tools.stream().map(this::quote)
                .collect(java.util.stream.Collectors.joining(",", "[", "]"));
        return "{\"workspaceRole\":" + role
                + ",\"tools\":" + toolJson
                + ",\"dataStatus\":" + quote(dataStatus)
                + ",\"recordCount\":" + recordCount
                + ",\"accessibleTeamCount\":" + accessibleTeamCount + "}";
    }

    private String quote(String value) {
        return "\"" + value.replace("\\", "\\\\").replace("\"", "\\\"") + "\"";
    }
}
