package com.factory.management.modules.ai.tool;

import com.factory.management.modules.ai.AiDataTool;
import com.factory.management.modules.ai.AiToolArguments;
import com.factory.management.modules.ai.AiToolContext;
import com.factory.management.modules.ai.AiToolResult;
import com.factory.management.modules.hr.entity.AttendanceStatus;
import com.factory.management.modules.masterdata.entity.DowntimeReasonType;
import com.factory.management.modules.production.entity.EmployeeActual;
import com.factory.management.modules.production.entity.MachineDowntime;
import com.factory.management.modules.production.entity.MaterialIssue;
import com.factory.management.modules.production.entity.ProductionReport;
import com.factory.management.modules.auth.entity.Role;
import com.factory.management.modules.production.repository.EmployeeActualRepository;
import com.factory.management.modules.production.repository.MachineDowntimeRepository;
import com.factory.management.modules.production.repository.MaterialIssueRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ProductivityAnalysisTool implements AiDataTool {
    private static final Set<Role> ALLOWED = Set.of(
            Role.ADMIN, Role.DIRECTOR, Role.FACTORY_MANAGER, Role.DEPARTMENT_MANAGER,
            Role.PRODUCTION_MANAGER, Role.TEAM_LEADER, Role.FINANCE
    );

    private final ScopedProductionQuery productionQuery;
    private final MachineDowntimeRepository downtimeRepository;
    private final MaterialIssueRepository materialIssueRepository;
    private final EmployeeActualRepository employeeActualRepository;

    @Override
    public String name() {
        return "analyze_productivity";
    }

    @Override
    public String description() {
        return "So sánh năng suất sản phẩm đạt trên giờ vận hành giữa hai kỳ, tìm đơn vị giảm mạnh "
                + "và các yếu tố có dữ liệu hỗ trợ như downtime, lỗi, vật tư và vắng mặt. "
                + "Không dùng cho câu hỏi sản lượng thực tế so với kế hoạch; trường hợp đó dùng get_production_summary. "
                + "Chỉ dùng dữ liệu chính thức.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("currentFrom", AiToolArguments.nullableString("Ngày bắt đầu kỳ hiện tại ISO yyyy-MM-dd; null là đầu năm hiện tại."));
        properties.put("currentTo", AiToolArguments.nullableString("Ngày kết thúc kỳ hiện tại; null là hôm nay."));
        properties.put("previousFrom", AiToolArguments.nullableString("Ngày bắt đầu kỳ so sánh; null là cùng ngày đầu của năm trước."));
        properties.put("previousTo", AiToolArguments.nullableString("Ngày kết thúc kỳ so sánh; null là cùng ngày cuối của năm trước."));
        properties.put("groupBy", AiToolArguments.requiredEnum(
                "Cấp cần phân tích.", List.of("FACTORY", "DEPARTMENT", "PRODUCTION_LINE", "TEAM", "MACHINE")));
        properties.put("factory", AiToolArguments.nullableString("Mã hoặc tên nhà máy; null nếu không lọc."));
        properties.put("department", AiToolArguments.nullableString("Mã hoặc tên phòng ban; null nếu không lọc."));
        properties.put("productionLine", AiToolArguments.nullableString("Mã hoặc tên dây chuyền; null nếu không lọc."));
        properties.put("team", AiToolArguments.nullableString("Mã hoặc tên tổ; null nếu không lọc."));
        properties.put("machine", AiToolArguments.nullableString("Mã hoặc tên máy; null nếu không lọc."));
        // Every argument has a safe runtime default. Advertising them as
        // required made small local models invent dates/grouping values.
        return AiToolArguments.objectSchema(properties, List.of());
    }

    @Override
    public Set<Role> allowedRoles() {
        return ALLOWED;
    }

    @Override
    @Transactional(readOnly = true)
    public AiToolResult execute(Map<String, Object> arguments, AiToolContext context) {
        LocalDate currentFrom = AiToolArguments.date(
                arguments, "currentFrom", LocalDate.of(context.today().getYear(), 1, 1));
        LocalDate currentTo = AiToolArguments.date(arguments, "currentTo", context.today());
        LocalDate previousFrom = AiToolArguments.date(arguments, "previousFrom", currentFrom.minusYears(1));
        LocalDate previousTo = AiToolArguments.date(arguments, "previousTo", currentTo.minusYears(1));
        AiToolArguments.validatePeriod(currentFrom, currentTo, 366);
        AiToolArguments.validatePeriod(previousFrom, previousTo, 366);
        GroupBy groupBy = parseGroupBy(AiToolArguments.text(arguments, "groupBy"));

        String factory = AiToolArguments.text(arguments, "factory");
        String department = AiToolArguments.text(arguments, "department");
        String line = AiToolArguments.text(arguments, "productionLine");
        String team = AiToolArguments.text(arguments, "team");
        String machine = AiToolArguments.text(arguments, "machine");

        List<ProductionReport> currentReports = productionQuery.find(
                context, currentFrom, currentTo, factory, department, line, team, machine);
        List<ProductionReport> previousReports = productionQuery.find(
                context, previousFrom, previousTo, factory, department, line, team, machine);
        Map<String, GroupMetrics> current = metrics(currentReports, groupBy);
        Map<String, GroupMetrics> previous = metrics(previousReports, groupBy);
        enrichDrivers(currentReports, current, groupBy);
        enrichDrivers(previousReports, previous, groupBy);

        Set<String> groupKeys = new LinkedHashSet<>();
        groupKeys.addAll(current.keySet());
        groupKeys.addAll(previous.keySet());
        List<Map<String, Object>> comparisons = groupKeys.stream()
                .map(key -> comparison(current.get(key), previous.get(key)))
                .sorted(Comparator.comparing(value -> nullableDecimal(value.get("productivityChangePercent"))))
                .limit(30)
                .toList();
        List<Map<String, Object>> declines = comparisons.stream()
                .filter(value -> value.get("productivityChangePercent") instanceof BigDecimal change
                        && change.signum() < 0)
                .limit(10)
                .toList();

        GroupMetrics currentTotal = total(current.values());
        GroupMetrics previousTotal = total(previous.values());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currentPeriod", Map.of("fromDate", currentFrom, "toDate", currentTo));
        data.put("previousPeriod", Map.of("fromDate", previousFrom, "toDate", previousTo));
        data.put("groupBy", groupBy.name());
        data.put("overall", comparison(currentTotal, previousTotal));
        data.put("topDeclines", declines);
        data.put("allComparedGroups", comparisons);

        List<String> warnings = new ArrayList<>();
        warnings.add("Các yếu tố được liệt kê là bằng chứng tương quan/đóng góp, không tự động chứng minh quan hệ nhân quả.");
        if (currentReports.isEmpty() && previousReports.isEmpty()) {
            warnings.add("Không có báo cáo sản xuất chính thức trong hai kỳ và phạm vi được phép xem.");
        }
        return new AiToolResult(
                name(), "OFFICIAL", data,
                List.of(new AiToolResult.Source(
                        "PRODUCTION_REPORT_AND_OFFICIAL_DETAILS",
                        "Báo cáo sản xuất và chi tiết chính thức",
                        "OFFICIAL", currentReports.size() + previousReports.size(), LocalDateTime.now())),
                warnings
        );
    }

    private Map<String, GroupMetrics> metrics(List<ProductionReport> reports, GroupBy groupBy) {
        Map<String, GroupMetrics> result = new LinkedHashMap<>();
        for (ProductionReport report : reports) {
            String key = key(report, groupBy);
            result.computeIfAbsent(key, ignored -> new GroupMetrics(
                    groupBy.name(), groupId(report, groupBy), groupCode(report, groupBy), groupName(report, groupBy)))
                    .add(report);
        }
        return result;
    }

    private GroupMetrics total(java.util.Collection<GroupMetrics> groups) {
        GroupMetrics total = new GroupMetrics("AUTHORIZED_SCOPE", null, null, "Phạm vi được phép xem");
        groups.forEach(total::merge);
        return total;
    }

    private void enrichDrivers(
            List<ProductionReport> reports,
            Map<String, GroupMetrics> metrics,
            GroupBy groupBy
    ) {
        if (reports.isEmpty()) return;
        Map<Long, String> reportGroups = reports.stream().collect(Collectors.toMap(
                ProductionReport::getId, report -> key(report, groupBy)));
        Set<Long> reportIds = reportGroups.keySet();
        for (MachineDowntime value : downtimeRepository.findAllByProductionReport_IdIn(reportIds)) {
            GroupMetrics target = metrics.get(reportGroups.get(value.getProductionReport().getId()));
            if (target == null) continue;
            if (value.getDowntimeReason().getReasonType() == DowntimeReasonType.UNPLANNED) {
                target.unplannedDowntimeMinutes += value.getDurationMinutes();
            }
        }
        for (MaterialIssue value : materialIssueRepository.findAllByProductionReport_IdIn(reportIds)) {
            GroupMetrics target = metrics.get(reportGroups.get(value.getProductionReport().getId()));
            if (target != null) target.materialIssueCount++;
        }
        for (EmployeeActual value : employeeActualRepository.findAllByProductionReport_IdIn(reportIds)) {
            GroupMetrics target = metrics.get(reportGroups.get(value.getProductionReport().getId()));
            if (target == null) continue;
            if (value.getAttendanceStatus() == AttendanceStatus.ABSENT
                    || value.getAttendanceStatus() == AttendanceStatus.ON_LEAVE) {
                target.absentOrLeaveCount++;
            }
            if (value.getAttendanceStatus() == AttendanceStatus.LATE
                    || value.getAttendanceStatus() == AttendanceStatus.LEAVE_EARLY) {
                target.lateOrEarlyCount++;
            }
            target.overtimeMinutes += value.getOvertimeMinutes();
        }
    }

    private Map<String, Object> comparison(GroupMetrics current, GroupMetrics previous) {
        GroupMetrics reference = current != null ? current : previous;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("scopeType", reference == null ? null : reference.scopeType);
        result.put("scopeId", reference == null ? null : reference.scopeId);
        result.put("scopeCode", reference == null ? null : reference.scopeCode);
        result.put("scopeName", reference == null ? null : reference.scopeName);
        result.put("current", current == null ? emptyMetrics() : current.toMap());
        result.put("previous", previous == null ? emptyMetrics() : previous.toMap());
        BigDecimal currentProductivity = current == null ? BigDecimal.ZERO : current.productivity();
        BigDecimal previousProductivity = previous == null ? BigDecimal.ZERO : previous.productivity();
        BigDecimal change = previousProductivity.signum() == 0 ? null
                : currentProductivity.subtract(previousProductivity).multiply(BigDecimal.valueOf(100))
                .divide(previousProductivity.abs(), 2, RoundingMode.HALF_UP);
        result.put("productivityChangePercent", change);
        result.put("possibleDrivers", drivers(current, previous));
        return result;
    }

    private List<String> drivers(GroupMetrics current, GroupMetrics previous) {
        if (current == null) return List.of("Không có dữ liệu kỳ hiện tại để phân tích.");
        if (previous == null) return List.of("Không có dữ liệu kỳ so sánh để xác định xu hướng.");
        List<String> result = new ArrayList<>();
        BigDecimal downtimeDelta = current.downtimeRate().subtract(previous.downtimeRate());
        BigDecimal defectDelta = current.defectRate().subtract(previous.defectRate());
        if (downtimeDelta.compareTo(new BigDecimal("0.50")) > 0) {
            result.add("Tỷ lệ downtime tăng " + downtimeDelta + " điểm phần trăm.");
        }
        if (current.unplannedDowntimeMinutes > previous.unplannedDowntimeMinutes) {
            result.add("Downtime ngoài kế hoạch tăng từ " + previous.unplannedDowntimeMinutes
                    + " lên " + current.unplannedDowntimeMinutes + " phút.");
        }
        if (defectDelta.compareTo(new BigDecimal("0.20")) > 0) {
            result.add("Tỷ lệ lỗi tăng " + defectDelta + " điểm phần trăm.");
        }
        if (current.materialIssueCount > previous.materialIssueCount) {
            result.add("Số sự cố vật tư tăng từ " + previous.materialIssueCount
                    + " lên " + current.materialIssueCount + ".");
        }
        if (current.absentOrLeaveCount > previous.absentOrLeaveCount) {
            result.add("Lượt vắng/nghỉ tăng từ " + previous.absentOrLeaveCount
                    + " lên " + current.absentOrLeaveCount + ".");
        }
        if (result.isEmpty()) result.add("Chưa thấy biến động nổi bật từ downtime, lỗi, vật tư hoặc hiện diện nhân sự.");
        return result;
    }

    private Map<String, Object> emptyMetrics() {
        return new GroupMetrics(null, null, null, null).toMap();
    }

    private BigDecimal nullableDecimal(Object value) {
        return value instanceof BigDecimal decimal ? decimal : new BigDecimal("999999");
    }

    private GroupBy parseGroupBy(String value) {
        if (value == null || value.isBlank()) return GroupBy.PRODUCTION_LINE;
        try {
            return GroupBy.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT)
                    .replace('-', '_')
                    .replace(' ', '_'));
        } catch (IllegalArgumentException exception) {
            // Grouping is presentational and never expands the JWT data scope.
            // Fall back deterministically instead of failing the whole chat.
            return GroupBy.PRODUCTION_LINE;
        }
    }

    private String key(ProductionReport value, GroupBy groupBy) {
        return groupBy.name() + ":" + groupId(value, groupBy);
    }

    private Long groupId(ProductionReport value, GroupBy groupBy) {
        return switch (groupBy) {
            case FACTORY -> value.getFactory().getId();
            case DEPARTMENT -> value.getDepartment().getId();
            case PRODUCTION_LINE -> value.getProductionLine().getId();
            case TEAM -> value.getTeam().getId();
            case MACHINE -> value.getMachine().getId();
        };
    }

    private String groupCode(ProductionReport value, GroupBy groupBy) {
        return switch (groupBy) {
            case FACTORY -> value.getFactory().getCode();
            case DEPARTMENT -> value.getDepartment().getCode();
            case PRODUCTION_LINE -> value.getProductionLine().getCode();
            case TEAM -> value.getTeam().getCode();
            case MACHINE -> value.getMachine().getCode();
        };
    }

    private String groupName(ProductionReport value, GroupBy groupBy) {
        return switch (groupBy) {
            case FACTORY -> value.getFactory().getName();
            case DEPARTMENT -> value.getDepartment().getName();
            case PRODUCTION_LINE -> value.getProductionLine().getName();
            case TEAM -> value.getTeam().getName();
            case MACHINE -> value.getMachine().getName();
        };
    }

    private enum GroupBy {
        FACTORY, DEPARTMENT, PRODUCTION_LINE, TEAM, MACHINE
    }

    private static final class GroupMetrics {
        private final String scopeType;
        private final Long scopeId;
        private final String scopeCode;
        private final String scopeName;
        private long reportCount;
        private long planned;
        private long actual;
        private long good;
        private long defect;
        private long workingMinutes;
        private long downtimeMinutes;
        private long unplannedDowntimeMinutes;
        private long materialIssueCount;
        private long absentOrLeaveCount;
        private long lateOrEarlyCount;
        private long overtimeMinutes;

        private GroupMetrics(String scopeType, Long scopeId, String scopeCode, String scopeName) {
            this.scopeType = scopeType;
            this.scopeId = scopeId;
            this.scopeCode = scopeCode;
            this.scopeName = scopeName;
        }

        private void add(ProductionReport report) {
            reportCount++;
            planned += report.getPlannedQuantity();
            actual += report.getActualQuantity();
            good += report.getGoodQuantity();
            defect += report.getDefectQuantity();
            workingMinutes += report.getWorkingMinutes();
            downtimeMinutes += report.getDowntimeMinutes();
        }

        private void merge(GroupMetrics value) {
            reportCount += value.reportCount;
            planned += value.planned;
            actual += value.actual;
            good += value.good;
            defect += value.defect;
            workingMinutes += value.workingMinutes;
            downtimeMinutes += value.downtimeMinutes;
            unplannedDowntimeMinutes += value.unplannedDowntimeMinutes;
            materialIssueCount += value.materialIssueCount;
            absentOrLeaveCount += value.absentOrLeaveCount;
            lateOrEarlyCount += value.lateOrEarlyCount;
            overtimeMinutes += value.overtimeMinutes;
        }

        private BigDecimal productivity() {
            long operating = Math.max(workingMinutes - downtimeMinutes, 0);
            if (operating == 0) return BigDecimal.ZERO.setScale(2);
            return BigDecimal.valueOf(good).multiply(BigDecimal.valueOf(60))
                    .divide(BigDecimal.valueOf(operating), 2, RoundingMode.HALF_UP);
        }

        private BigDecimal downtimeRate() {
            return percent(downtimeMinutes, workingMinutes);
        }

        private BigDecimal defectRate() {
            return percent(defect, actual);
        }

        private Map<String, Object> toMap() {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("reportCount", reportCount);
            value.put("plannedQuantity", planned);
            value.put("actualQuantity", actual);
            value.put("goodQuantity", good);
            value.put("defectQuantity", defect);
            value.put("workingMinutes", workingMinutes);
            value.put("downtimeMinutes", downtimeMinutes);
            value.put("productivityGoodUnitsPerOperatingHour", productivity());
            value.put("planAttainmentPercent", percent(actual, planned));
            value.put("downtimeRatePercent", downtimeRate());
            value.put("defectRatePercent", defectRate());
            value.put("unplannedDowntimeMinutes", unplannedDowntimeMinutes);
            value.put("materialIssueCount", materialIssueCount);
            value.put("absentOrLeaveCount", absentOrLeaveCount);
            value.put("lateOrEarlyCount", lateOrEarlyCount);
            value.put("overtimeMinutes", overtimeMinutes);
            return value;
        }

        private static BigDecimal percent(long numerator, long denominator) {
            if (denominator <= 0) return BigDecimal.ZERO.setScale(2);
            return BigDecimal.valueOf(numerator).multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP);
        }
    }
}
