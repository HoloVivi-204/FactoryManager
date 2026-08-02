package com.factory.management.modules.ai;

import java.util.List;
import java.util.Map;

public record AiDashboard(
        String title,
        String subtitle,
        List<Kpi> kpis,
        List<Bar> bars,
        List<Column> columns,
        List<Map<String, Object>> rows,
        List<Ratio> ratios,
        List<Widget> widgets
) {
    public AiDashboard(
            String title,
            String subtitle,
            List<Kpi> kpis,
            List<Bar> bars,
            List<Column> columns,
            List<Map<String, Object>> rows
    ) {
        this(title, subtitle, kpis, bars, columns, rows, List.of(), List.of());
    }

    public AiDashboard(
            String title,
            String subtitle,
            List<Kpi> kpis,
            List<Bar> bars,
            List<Column> columns,
            List<Map<String, Object>> rows,
            List<Ratio> ratios
    ) {
        this(title, subtitle, kpis, bars, columns, rows, ratios, List.of());
    }

    public record Kpi(String label, Object value, String unit, String tone) {
    }

    public record Bar(String label, Number value, String unit) {
    }

    public record Column(String key, String label, String format) {
    }

    public record Ratio(
            String key,
            String label,
            Number numerator,
            Number denominator,
            String unit,
            String numeratorLabel,
            String denominatorLabel,
            String context,
            String tone
    ) {
    }

    public record Widget(
            String viewType,
            String title,
            String context,
            String categoryKey,
            List<Series> series,
            List<Column> columns,
            List<Map<String, Object>> rows,
            Number numerator,
            Number denominator,
            String unit,
            String numeratorLabel,
            String denominatorLabel,
            String tone
    ) {
    }

    public record Series(
            String key,
            String label,
            String unit,
            String tone
    ) {
    }
}
