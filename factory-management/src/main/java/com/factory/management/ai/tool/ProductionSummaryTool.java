package com.factory.management.ai.tool;

import com.factory.management.ai.AiDataTool;
import com.factory.management.ai.AiToolArguments;
import com.factory.management.ai.AiToolContext;
import com.factory.management.ai.AiToolResult;
import com.factory.management.entity.ProductionReport;
import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.ProductionReportStatus;
import com.factory.management.entity.Role;
import com.factory.management.repository.ProductionReportRepository;
import com.factory.management.repository.ProductionReportStagingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductionSummaryTool implements AiDataTool {
    private static final Set<Role> ALLOWED = Set.of(
            Role.ADMIN, Role.DIRECTOR, Role.FACTORY_MANAGER, Role.DEPARTMENT_MANAGER,
            Role.PRODUCTION_MANAGER, Role.TEAM_LEADER, Role.FINANCE
    );
    private static final Set<Role> TEMPORARY_ALLOWED = Set.of(
            Role.ADMIN, Role.DIRECTOR, Role.FACTORY_MANAGER, Role.DEPARTMENT_MANAGER,
            Role.PRODUCTION_MANAGER, Role.TEAM_LEADER
    );

    private final ScopedProductionQuery productionQuery;
    private final ProductionReportStagingRepository stagingRepository;
    private final ProductionReportRepository officialRepository;

    @Override
    public String name() {
        return "get_production_summary";
    }

    @Override
    public String description() {
        return "Tổng hợp sản lượng, hàng đạt, hàng lỗi, thời gian dừng và hiệu suất trong phạm vi được cấp. "
                + "Dùng cả khi người dùng hỏi thực tế so với kế hoạch và xin cách cải thiện/tăng sản lượng. "
                + "Mặc định chỉ dùng ProductionReport chính thức; dữ liệu tạm luôn được trả riêng và gắn nhãn.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("fromDate", AiToolArguments.nullableString("Ngày bắt đầu ISO yyyy-MM-dd; null nghĩa là hôm nay."));
        properties.put("toDate", AiToolArguments.nullableString("Ngày kết thúc ISO yyyy-MM-dd; null nghĩa là fromDate."));
        properties.put("factory", AiToolArguments.nullableString("Mã hoặc tên nhà máy; null nếu không lọc."));
        properties.put("department", AiToolArguments.nullableString("Mã hoặc tên phòng ban; null nếu không lọc."));
        properties.put("productionLine", AiToolArguments.nullableString("Mã hoặc tên dây chuyền; null nếu không lọc."));
        properties.put("team", AiToolArguments.nullableString("Mã hoặc tên tổ; null nếu không lọc."));
        properties.put("machine", AiToolArguments.nullableString("Mã hoặc tên máy; null nếu không lọc."));
        properties.put("includeTemporary", AiToolArguments.nullableBoolean(
                "true chỉ khi người dùng hỏi rõ dữ liệu nháp/chưa chốt; mặc định false."));
        return AiToolArguments.objectSchema(properties);
    }

    @Override
    public Set<Role> allowedRoles() {
        return ALLOWED;
    }

    @Override
    @Transactional(readOnly = true)
    public AiToolResult execute(Map<String, Object> arguments, AiToolContext context) {
        LocalDate from = AiToolArguments.date(arguments, "fromDate", context.today());
        LocalDate to = AiToolArguments.date(arguments, "toDate", from);
        AiToolArguments.validatePeriod(from, to, 366);

        String factory = AiToolArguments.text(arguments, "factory");
        String department = AiToolArguments.text(arguments, "department");
        String line = AiToolArguments.text(arguments, "productionLine");
        String team = AiToolArguments.text(arguments, "team");
        String machine = AiToolArguments.text(arguments, "machine");

        List<ProductionReport> official = productionQuery.find(
                context, from, to, factory, department, line, team, machine);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("period", Map.of("fromDate", from, "toDate", to));
        data.put("official", officialMetrics(official));
        data.put("breakdownByProductionLine", lineBreakdown(official));

        List<AiToolResult.Source> sources = new ArrayList<>();
        sources.add(new AiToolResult.Source(
                "PRODUCTION_REPORT", "Báo cáo sản xuất chính thức", "OFFICIAL",
                official.size(), LocalDateTime.now()));
        List<String> warnings = new ArrayList<>();
        String dataStatus = "OFFICIAL";

        boolean requestedTemporary = AiToolArguments.bool(arguments, "includeTemporary", false);
        boolean mayReadTemporary = context.effectiveRoles().stream().anyMatch(TEMPORARY_ALLOWED::contains);
        if (requestedTemporary && mayReadTemporary) {
            List<ProductionReportStaging> temporary = temporary(
                    context, from, to, factory, department, line, team, machine);
            data.put("temporaryUnconfirmed", stagingMetrics(temporary));
            data.put("temporaryStatusCounts", statusCounts(temporary));
            sources.add(new AiToolResult.Source(
                    "PRODUCTION_REPORT_STAGING", "Báo cáo sản xuất tạm/chưa xác nhận",
                    "TEMPORARY_UNCONFIRMED", temporary.size(), LocalDateTime.now()));
            if (!temporary.isEmpty()) {
                dataStatus = official.isEmpty() ? "TEMPORARY_UNCONFIRMED" : "OFFICIAL_WITH_TEMPORARY";
                warnings.add("Dữ liệu tạm/chưa xác nhận được trình bày riêng và không cộng vào số liệu chính thức.");
            }
        } else if (requestedTemporary) {
            warnings.add("Vai trò hiện tại không được xem dữ liệu báo cáo tạm.");
        }
        if (context.accessibleTeamIds().isEmpty()) {
            warnings.add("Tài khoản chưa có phạm vi tổ/dây chuyền để xem dữ liệu vận hành.");
        }
        return new AiToolResult(name(), dataStatus, data, sources, warnings);
    }

    private List<ProductionReportStaging> temporary(
            AiToolContext context,
            LocalDate from,
            LocalDate to,
            String factory,
            String department,
            String line,
            String team,
            String machine
    ) {
        if (context.accessibleTeamIds().isEmpty()) return List.of();
        return stagingRepository
                .findAllByTeam_IdInAndReportDateBetweenOrderByReportDateDescIdDesc(
                        context.accessibleTeamIds(), from, to)
                .stream()
                .filter(value -> value.getStatus() != ProductionReportStatus.IMPORTED
                        && value.getStatus() != ProductionReportStatus.LOCKED)
                .filter(value -> !officialRepository.existsBySourceStaging_Id(value.getId()))
                .filter(value -> matches(value.getFactory().getCode(), value.getFactory().getName(), factory))
                .filter(value -> matches(value.getDepartment().getCode(), value.getDepartment().getName(), department))
                .filter(value -> matches(value.getProductionLine().getCode(), value.getProductionLine().getName(), line))
                .filter(value -> matches(value.getTeam().getCode(), value.getTeam().getName(), team))
                .filter(value -> matches(value.getMachine().getCode(), value.getMachine().getName(), machine))
                .toList();
    }

    private boolean matches(String code, String name, String selector) {
        if (selector == null || selector.isBlank()) return true;
        String value = AiToolArguments.normalized(selector);
        return AiToolArguments.normalized(code).contains(value)
                || AiToolArguments.normalized(name).contains(value);
    }

    private Map<String, Object> officialMetrics(List<ProductionReport> reports) {
        long planned = reports.stream().mapToLong(ProductionReport::getPlannedQuantity).sum();
        long actual = reports.stream().mapToLong(ProductionReport::getActualQuantity).sum();
        long good = reports.stream().mapToLong(ProductionReport::getGoodQuantity).sum();
        long defect = reports.stream().mapToLong(ProductionReport::getDefectQuantity).sum();
        long working = reports.stream().mapToLong(ProductionReport::getWorkingMinutes).sum();
        long downtime = reports.stream().mapToLong(ProductionReport::getDowntimeMinutes).sum();
        return metricMap(reports.size(), planned, actual, good, defect, working, downtime);
    }

    private Map<String, Object> stagingMetrics(List<ProductionReportStaging> reports) {
        long planned = reports.stream().mapToLong(ProductionReportStaging::getPlannedQuantity).sum();
        long actual = reports.stream().mapToLong(ProductionReportStaging::getActualQuantity).sum();
        long good = reports.stream().mapToLong(ProductionReportStaging::getGoodQuantity).sum();
        long defect = reports.stream().mapToLong(ProductionReportStaging::getDefectQuantity).sum();
        long working = reports.stream().mapToLong(ProductionReportStaging::getWorkingMinutes).sum();
        long downtime = reports.stream().mapToLong(ProductionReportStaging::getDowntimeMinutes).sum();
        return metricMap(reports.size(), planned, actual, good, defect, working, downtime);
    }

    private Map<String, Object> metricMap(
            long count,
            long planned,
            long actual,
            long good,
            long defect,
            long working,
            long downtime
    ) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("reportCount", count);
        result.put("plannedQuantity", planned);
        result.put("actualQuantity", actual);
        result.put("goodQuantity", good);
        result.put("defectQuantity", defect);
        result.put("workingMinutes", working);
        result.put("downtimeMinutes", downtime);
        result.put("planAttainmentPercent", percent(actual, planned));
        result.put("availabilityPercent", percent(Math.max(working - downtime, 0), working));
        result.put("qualityYieldPercent", percent(good, actual));
        result.put("defectRatePercent", percent(defect, actual));
        return result;
    }

    private List<Map<String, Object>> lineBreakdown(List<ProductionReport> reports) {
        return reports.stream().collect(Collectors.groupingBy(
                        report -> report.getProductionLine().getId(), LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .sorted(Comparator.comparingLong((List<ProductionReport> values) ->
                        values.stream().mapToLong(ProductionReport::getActualQuantity).sum()).reversed())
                .limit(20)
                .map(values -> {
                    ProductionReport first = values.get(0);
                    Map<String, Object> item = new LinkedHashMap<>();
                    item.put("productionLineId", first.getProductionLine().getId());
                    item.put("code", first.getProductionLine().getCode());
                    item.put("name", first.getProductionLine().getName());
                    item.putAll(officialMetrics(values));
                    return item;
                }).toList();
    }

    private Map<String, Long> statusCounts(List<ProductionReportStaging> reports) {
        Map<ProductionReportStatus, Long> counts = reports.stream().collect(Collectors.groupingBy(
                ProductionReportStaging::getStatus,
                () -> new EnumMap<>(ProductionReportStatus.class),
                Collectors.counting()));
        Map<String, Long> result = new LinkedHashMap<>();
        counts.forEach((key, value) -> result.put(key.name(), value));
        return result;
    }

    private BigDecimal percent(long numerator, long denominator) {
        if (denominator <= 0) return BigDecimal.ZERO.setScale(2);
        return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
    }
}
