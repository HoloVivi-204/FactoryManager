package com.factory.management.ai;

import java.util.List;

/**
 * The model may select a presentation, but it may only reference fields returned
 * by an authorized Tool. AiVisualizationService resolves and validates every
 * reference before any value is sent to the browser.
 */
public record AiVisualizationSpec(
        String title,
        String subtitle,
        List<Widget> widgets
) {
    public static AiVisualizationSpec empty() {
        return new AiVisualizationSpec("", "", List.of());
    }

    public record Widget(
            String type,
            String title,
            String toolName,
            String dataPath,
            String categoryField,
            List<Series> series,
            String numeratorPath,
            String denominatorPath,
            String unit,
            List<Column> columns
    ) {
    }

    public record Series(
            String field,
            String label,
            String tone
    ) {
    }

    public record Column(
            String field,
            String label,
            String format
    ) {
    }
}
