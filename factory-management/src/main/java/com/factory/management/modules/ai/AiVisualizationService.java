package com.factory.management.modules.ai;

import java.math.BigDecimal;
import java.time.temporal.TemporalAccessor;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class AiVisualizationService {
    private static final Pattern SAFE_PATH = Pattern.compile(
            "[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)*");
    private static final Pattern SAFE_FIELD = Pattern.compile("[A-Za-z][A-Za-z0-9_]*");
    private static final Set<String> CHART_TYPES = Set.of("GROUPED_BAR", "LINE", "DONUT");
    private static final Set<String> FORMATS = Set.of(
            "text", "number", "percent", "date", "datetime", "status", "duration", "currency");

    public List<AiDashboard> materialize(
            AiVisualizationSpec specification,
            List<AiToolResult> results
    ) {
        Map<String, List<AiVisualizationSpec.Widget>> plannedByTool = new LinkedHashMap<>();
        if (specification != null && specification.widgets() != null) {
            specification.widgets().stream().limit(6).forEach(widget -> {
                if (widget != null && text(widget.toolName(), 80) != null) {
                    plannedByTool.computeIfAbsent(widget.toolName(), ignored -> new ArrayList<>()).add(widget);
                }
            });
        }

        List<AiDashboard> dashboards = new ArrayList<>();
        for (AiToolResult result : results) {
            if (result.dashboard() != null) {
                dashboards.add(result.dashboard());
                continue;
            }
            if ("NO_BUSINESS_DATA".equals(result.dataStatus())) continue;

            AiDashboard generated = fromSpecification(
                    specification,
                    result,
                    plannedByTool.getOrDefault(result.toolName(), List.of()));
            if (generated == null) generated = fallback(result);
            if (generated != null) dashboards.add(generated);
        }
        return List.copyOf(dashboards);
    }

    private AiDashboard fromSpecification(
            AiVisualizationSpec specification,
            AiToolResult result,
            List<AiVisualizationSpec.Widget> planned
    ) {
        if (planned.isEmpty()) return null;
        List<AiDashboard.Widget> widgets = planned.stream()
                .limit(6)
                .map(widget -> materializeWidget(result, widget))
                .filter(java.util.Objects::nonNull)
                .toList();
        if (widgets.isEmpty()) return null;

        return dashboard(
                text(specification.title(), 120) == null
                        ? defaultTitle(result.toolName()) : text(specification.title(), 120),
                text(specification.subtitle(), 240) == null
                        ? periodLabel(result.data()) : text(specification.subtitle(), 240),
                widgets);
    }

    private AiDashboard.Widget materializeWidget(
            AiToolResult result,
            AiVisualizationSpec.Widget specification
    ) {
        String type = text(specification.type(), 30);
        if (type == null) return null;
        type = type.toUpperCase(java.util.Locale.ROOT);
        String title = text(specification.title(), 100);
        String unit = text(specification.unit(), 40);

        if ("RATIO".equals(type) || "PROGRESS".equals(type)) {
            Number numerator = number(resolve(result.data(), specification.numeratorPath()));
            Number denominator = number(resolve(result.data(), specification.denominatorPath()));
            if (numerator == null || denominator == null || decimal(denominator).signum() <= 0) return null;
            return new AiDashboard.Widget(
                    type,
                    title == null ? "Tỷ lệ trên tổng" : title,
                    periodLabel(result.data()),
                    null, List.of(), List.of(), List.of(),
                    numerator, denominator, unit,
                    "Thực tế", "Tổng/Kế hoạch",
                    decimal(numerator).compareTo(decimal(denominator)) >= 0 ? "green" : "blue");
        }

        Object rawRows = resolve(result.data(), specification.dataPath());
        List<Map<String, Object>> sourceRows = mapRows(rawRows, 100);
        if (sourceRows.isEmpty()) return null;

        if ("TABLE".equals(type)) {
            List<AiDashboard.Column> columns = columns(specification.columns(), sourceRows);
            if (columns.isEmpty()) return null;
            return new AiDashboard.Widget(
                    type,
                    title == null ? "Bảng dữ liệu chi tiết" : title,
                    periodLabel(result.data()),
                    null, List.of(), columns, selectRows(sourceRows, columns, 100),
                    null, null, unit, null, null, "blue");
        }

        if (!CHART_TYPES.contains(type)) return null;
        String category = safeField(specification.categoryField());
        if (category == null || sourceRows.stream().noneMatch(row -> scalar(row.get(category)))) return null;
        List<AiDashboard.Series> series = series(specification.series(), sourceRows, "DONUT".equals(type) ? 1 : 4);
        if (series.isEmpty()) return null;
        if ("DONUT".equals(type) && sourceRows.stream().anyMatch(row -> {
            Number value = number(row.get(series.get(0).key()));
            return value == null || decimal(value).signum() < 0;
        })) return null;

        List<String> fields = new ArrayList<>();
        fields.add(category);
        series.forEach(value -> fields.add(value.key()));
        List<Map<String, Object>> rows = selectRows(sourceRows, fields, 50);
        return new AiDashboard.Widget(
                type,
                title == null ? "Biểu đồ dữ liệu" : title,
                periodLabel(result.data()),
                category, series, List.of(), rows,
                null, null, unit, null, null, "blue");
    }

    private AiDashboard fallback(AiToolResult result) {
        return switch (result.toolName()) {
            case "get_production_summary" -> productionFallback(result);
            case "rank_maintenance_cost" -> maintenanceFallback(result);
            case "compare_financial_periods" -> financialFallback(result);
            case "analyze_productivity" -> productivityFallback(result);
            default -> null;
        };
    }

    private AiDashboard productionFallback(AiToolResult result) {
        Map<String, Object> official = map(result.data().get("official"));
        Number planned = number(official.get("plannedQuantity"));
        Number actual = number(official.get("actualQuantity"));
        List<AiDashboard.Widget> widgets = new ArrayList<>();
        if (planned != null && actual != null && decimal(planned).signum() > 0) {
            widgets.add(new AiDashboard.Widget(
                    "PROGRESS", "Sản lượng thực tế so với kế hoạch", periodLabel(result.data()),
                    null, List.of(), List.of(), List.of(), actual, planned, "sản phẩm",
                    "Thực tế", "Kế hoạch",
                    decimal(actual).compareTo(decimal(planned)) >= 0 ? "green" : "blue"));
        }

        List<Map<String, Object>> breakdown = mapRows(result.data().get("breakdownByProductionLine"), 100);
        if (!breakdown.isEmpty()) {
            List<AiDashboard.Column> columns = List.of(
                    new AiDashboard.Column("code", "Mã dây chuyền", "text"),
                    new AiDashboard.Column("name", "Dây chuyền", "text"),
                    new AiDashboard.Column("plannedQuantity", "Kế hoạch", "number"),
                    new AiDashboard.Column("actualQuantity", "Thực tế", "number"),
                    new AiDashboard.Column("planAttainmentPercent", "Tỷ lệ đạt", "percent"),
                    new AiDashboard.Column("goodQuantity", "Hàng đạt", "number"),
                    new AiDashboard.Column("defectQuantity", "Hàng lỗi", "number"),
                    new AiDashboard.Column("downtimeMinutes", "Dừng máy", "duration")
            );
            widgets.add(new AiDashboard.Widget(
                    "GROUPED_BAR", "Kế hoạch và thực tế theo dây chuyền", periodLabel(result.data()),
                    "name",
                    List.of(
                            new AiDashboard.Series("plannedQuantity", "Kế hoạch", "sản phẩm", "gray"),
                            new AiDashboard.Series("actualQuantity", "Thực tế", "sản phẩm", "blue")),
                    List.of(), selectRows(breakdown, List.of("name", "plannedQuantity", "actualQuantity"), 20),
                    null, null, "sản phẩm", null, null, "blue"));
            widgets.add(new AiDashboard.Widget(
                    "TABLE", "Chi tiết theo dây chuyền", periodLabel(result.data()),
                    null, List.of(), columns, selectRows(breakdown, columns, 100),
                    null, null, null, null, null, "blue"));
        }
        if (widgets.isEmpty()) return null;
        return dashboard("Dashboard sản lượng", periodLabel(result.data()), widgets);
    }

    private AiDashboard maintenanceFallback(AiToolResult result) {
        List<Map<String, Object>> machines = mapRows(result.data().get("machines"), 50);
        if (machines.isEmpty()) return null;
        List<AiDashboard.Column> columns = List.of(
                new AiDashboard.Column("machineCode", "Mã máy", "text"),
                new AiDashboard.Column("machineName", "Máy", "text"),
                new AiDashboard.Column("teamName", "Tổ", "text"),
                new AiDashboard.Column("workOrderCount", "Phiếu bảo trì", "number"),
                new AiDashboard.Column("laborCost", "Nhân công", "currency"),
                new AiDashboard.Column("partCost", "Vật tư", "currency"),
                new AiDashboard.Column("externalCost", "Thuê ngoài", "currency"),
                new AiDashboard.Column("totalCost", "Tổng chi phí", "currency")
        );
        return dashboard("Chi phí bảo trì theo máy", periodLabel(result.data()), List.of(
                new AiDashboard.Widget(
                        "GROUPED_BAR", "Xếp hạng tổng chi phí bảo trì", periodLabel(result.data()),
                        "machineName", List.of(new AiDashboard.Series(
                        "totalCost", "Tổng chi phí", "đ", "orange")),
                        List.of(), selectRows(machines, List.of("machineName", "totalCost"), 20),
                        null, null, "đ", null, null, "orange"),
                new AiDashboard.Widget(
                        "TABLE", "Chi tiết chi phí theo máy", periodLabel(result.data()),
                        null, List.of(), columns, selectRows(machines, columns, 50),
                        null, null, null, null, null, "blue")
        ));
    }

    private AiDashboard financialFallback(AiToolResult result) {
        Map<String, Object> current = map(result.data().get("current"));
        Map<String, Object> previous = map(result.data().get("previous"));
        if (current.isEmpty() && previous.isEmpty()) return null;
        List<Map<String, Object>> rows = new ArrayList<>();
        addComparisonRow(rows, "Doanh thu", "revenue", current, previous);
        addComparisonRow(rows, "Chi phí", "expense", current, previous);
        addComparisonRow(rows, "Lợi nhuận", "profit", current, previous);
        addComparisonRow(rows, "Phải thu", "accountsReceivable", current, previous);
        addComparisonRow(rows, "Phải trả", "accountsPayable", current, previous);
        List<AiDashboard.Column> columns = List.of(
                new AiDashboard.Column("metric", "Chỉ tiêu", "text"),
                new AiDashboard.Column("previous", "Kỳ trước", "currency"),
                new AiDashboard.Column("current", "Kỳ hiện tại", "currency"),
                new AiDashboard.Column("difference", "Chênh lệch", "currency")
        );
        return dashboard("So sánh tài chính giữa hai kỳ", comparisonPeriodLabel(result.data()), List.of(
                new AiDashboard.Widget(
                        "GROUPED_BAR", "Kỳ hiện tại so với kỳ trước", comparisonPeriodLabel(result.data()),
                        "metric", List.of(
                        new AiDashboard.Series("previous", "Kỳ trước", "đ", "gray"),
                        new AiDashboard.Series("current", "Kỳ hiện tại", "đ", "blue")),
                        List.of(), rows, null, null, "đ", null, null, "blue"),
                new AiDashboard.Widget(
                        "TABLE", "Chi tiết biến động tài chính", comparisonPeriodLabel(result.data()),
                        null, List.of(), columns, rows, null, null, null, null, null, "blue")
        ));
    }

    private AiDashboard productivityFallback(AiToolResult result) {
        List<Map<String, Object>> groups = mapRows(result.data().get("allComparedGroups"), 50);
        if (groups.isEmpty()) return null;
        List<Map<String, Object>> rows = new ArrayList<>();
        for (Map<String, Object> group : groups) {
            Map<String, Object> current = map(group.get("current"));
            Map<String, Object> previous = map(group.get("previous"));
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("scopeCode", group.get("scopeCode"));
            row.put("scopeName", group.get("scopeName"));
            row.put("previousProductivity", previous.get("productivityGoodUnitsPerOperatingHour"));
            row.put("currentProductivity", current.get("productivityGoodUnitsPerOperatingHour"));
            row.put("changePercent", group.get("productivityChangePercent"));
            row.put("downtimeRatePercent", current.get("downtimeRatePercent"));
            row.put("defectRatePercent", current.get("defectRatePercent"));
            row.put("possibleDrivers", group.get("possibleDrivers"));
            rows.add(row);
        }
        List<AiDashboard.Column> columns = List.of(
                new AiDashboard.Column("scopeCode", "Mã", "text"),
                new AiDashboard.Column("scopeName", "Đơn vị", "text"),
                new AiDashboard.Column("previousProductivity", "Năng suất kỳ trước", "number"),
                new AiDashboard.Column("currentProductivity", "Năng suất hiện tại", "number"),
                new AiDashboard.Column("changePercent", "Biến động", "percent"),
                new AiDashboard.Column("downtimeRatePercent", "Tỷ lệ dừng", "percent"),
                new AiDashboard.Column("defectRatePercent", "Tỷ lệ lỗi", "percent"),
                new AiDashboard.Column("possibleDrivers", "Yếu tố liên quan", "text")
        );
        return dashboard("So sánh năng suất", comparisonPeriodLabel(result.data()), List.of(
                new AiDashboard.Widget(
                        "GROUPED_BAR", "Năng suất hiện tại và kỳ trước", comparisonPeriodLabel(result.data()),
                        "scopeName", List.of(
                        new AiDashboard.Series("previousProductivity", "Kỳ trước", "SP tốt/giờ", "gray"),
                        new AiDashboard.Series("currentProductivity", "Hiện tại", "SP tốt/giờ", "blue")),
                        List.of(), selectRows(rows,
                        List.of("scopeName", "previousProductivity", "currentProductivity"), 30),
                        null, null, "SP tốt/giờ", null, null, "blue"),
                new AiDashboard.Widget(
                        "TABLE", "Chi tiết và yếu tố liên quan", comparisonPeriodLabel(result.data()),
                        null, List.of(), columns, selectRows(rows, columns, 50),
                        null, null, null, null, null, "blue")
        ));
    }

    private void addComparisonRow(
            List<Map<String, Object>> rows,
            String label,
            String field,
            Map<String, Object> current,
            Map<String, Object> previous
    ) {
        Number currentValue = number(current.get(field));
        Number previousValue = number(previous.get(field));
        BigDecimal currentDecimal = currentValue == null ? BigDecimal.ZERO : decimal(currentValue);
        BigDecimal previousDecimal = previousValue == null ? BigDecimal.ZERO : decimal(previousValue);
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("metric", label);
        row.put("previous", previousDecimal);
        row.put("current", currentDecimal);
        row.put("difference", currentDecimal.subtract(previousDecimal));
        rows.add(row);
    }

    private AiDashboard dashboard(String title, String subtitle, List<AiDashboard.Widget> widgets) {
        return new AiDashboard(
                title, subtitle == null ? "Dữ liệu trong phạm vi được cấp" : subtitle,
                List.of(), List.of(), List.of(), List.of(), List.of(), widgets);
    }

    private List<AiDashboard.Series> series(
            List<AiVisualizationSpec.Series> specifications,
            List<Map<String, Object>> rows,
            int limit
    ) {
        if (specifications == null) return List.of();
        List<AiDashboard.Series> result = new ArrayList<>();
        for (AiVisualizationSpec.Series specification : specifications) {
            if (result.size() >= limit || specification == null) break;
            String field = safeField(specification.field());
            if (field == null || rows.stream().noneMatch(row -> number(row.get(field)) != null)) continue;
            String label = text(specification.label(), 80);
            result.add(new AiDashboard.Series(
                    field, label == null ? field : label, null, text(specification.tone(), 20)));
        }
        return result;
    }

    private List<AiDashboard.Column> columns(
            List<AiVisualizationSpec.Column> specifications,
            List<Map<String, Object>> rows
    ) {
        if (specifications == null) return List.of();
        List<AiDashboard.Column> result = new ArrayList<>();
        Set<String> seen = new LinkedHashSet<>();
        for (AiVisualizationSpec.Column specification : specifications) {
            if (result.size() >= 8 || specification == null) break;
            String field = safeField(specification.field());
            if (field == null || !seen.add(field) || rows.stream().noneMatch(row -> row.containsKey(field))) continue;
            String label = text(specification.label(), 80);
            String format = text(specification.format(), 20);
            result.add(new AiDashboard.Column(
                    field, label == null ? field : label,
                    format != null && FORMATS.contains(format.toLowerCase(java.util.Locale.ROOT))
                            ? format.toLowerCase(java.util.Locale.ROOT) : inferFormat(rows, field)));
        }
        return result;
    }

    private String inferFormat(List<Map<String, Object>> rows, String field) {
        Object example = rows.stream().map(row -> row.get(field)).filter(java.util.Objects::nonNull)
                .findFirst().orElse(null);
        if (example instanceof Number) return "number";
        if (example instanceof TemporalAccessor) return "date";
        String lower = field.toLowerCase(java.util.Locale.ROOT);
        if (lower.contains("percent") || lower.contains("rate")) return "percent";
        if (lower.contains("date") || lower.endsWith("at") || lower.endsWith("time")) return "date";
        return "text";
    }

    private List<Map<String, Object>> selectRows(
            List<Map<String, Object>> rows,
            List<AiDashboard.Column> columns,
            int limit
    ) {
        return selectRows(rows, columns.stream().map(AiDashboard.Column::key).toList(), limit);
    }

    private List<Map<String, Object>> selectRows(
            List<Map<String, Object>> rows,
            java.util.Collection<String> fields,
            int limit
    ) {
        List<Map<String, Object>> selected = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            if (selected.size() >= limit) break;
            Map<String, Object> copy = new LinkedHashMap<>();
            for (String field : fields) copy.put(field, row.get(field));
            selected.add(copy);
        }
        return List.copyOf(selected);
    }

    private List<Map<String, Object>> mapRows(Object value, int limit) {
        return AiPayloads.objectMaps(value, limit);
    }

    private Map<String, Object> map(Object value) {
        return AiPayloads.objectMap(value);
    }

    private Object resolve(Map<String, Object> root, String rawPath) {
        String path = text(rawPath, 160);
        if (path == null) return root;
        if (!SAFE_PATH.matcher(path).matches()) return null;
        Object current = root;
        for (String segment : path.split("\\.")) {
            if (!(current instanceof Map<?, ?> map)) return null;
            current = map.get(segment);
        }
        return current;
    }

    private String safeField(String value) {
        String result = text(value, 80);
        return result != null && SAFE_FIELD.matcher(result).matches() ? result : null;
    }

    private String text(String value, int maxLength) {
        if (value == null) return null;
        String result = value.strip();
        if (result.isEmpty()) return null;
        return result.length() > maxLength ? result.substring(0, maxLength) : result;
    }

    private boolean scalar(Object value) {
        return value == null || value instanceof String || value instanceof Number
                || value instanceof Boolean || value instanceof TemporalAccessor || value.getClass().isEnum();
    }

    private Number number(Object value) {
        if (value instanceof Number number) return number;
        if (value == null) return null;
        try {
            return new BigDecimal(String.valueOf(value));
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private BigDecimal decimal(Number value) {
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal(value.toString());
    }

    private String defaultTitle(String toolName) {
        return switch (toolName) {
            case "get_production_summary" -> "Dashboard sản lượng";
            case "get_operational_details" -> "Dashboard vận hành";
            case "rank_maintenance_cost" -> "Dashboard chi phí bảo trì";
            case "compare_financial_periods" -> "Dashboard tài chính";
            case "analyze_productivity" -> "Dashboard năng suất";
            default -> "Dashboard dữ liệu";
        };
    }

    private String periodLabel(Map<String, Object> data) {
        Map<String, Object> period = map(data.get("period"));
        if (period.isEmpty()) return comparisonPeriodLabel(data);
        Object from = period.get("fromDate");
        Object to = period.get("toDate");
        if (from == null && to == null) return null;
        return java.util.Objects.equals(from, to)
                ? "Ngày " + from : "Từ " + from + " đến " + to;
    }

    private String comparisonPeriodLabel(Map<String, Object> data) {
        Map<String, Object> current = map(data.get("currentPeriod"));
        Map<String, Object> previous = map(data.get("previousPeriod"));
        if (current.isEmpty() && previous.isEmpty()) return null;
        return "Hiện tại " + range(current) + " · So sánh " + range(previous);
    }

    private String range(Map<String, Object> period) {
        Object from = period.get("fromDate");
        Object to = period.get("toDate");
        return java.util.Objects.equals(from, to) ? String.valueOf(from) : from + "–" + to;
    }
}
