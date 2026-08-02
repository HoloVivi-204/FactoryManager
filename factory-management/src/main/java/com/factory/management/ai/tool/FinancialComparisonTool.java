package com.factory.management.ai.tool;

import com.factory.management.ai.AiDataTool;
import com.factory.management.ai.AiToolArguments;
import com.factory.management.ai.AiToolContext;
import com.factory.management.ai.AiToolResult;
import com.factory.management.entity.ExpenseGroup;
import com.factory.management.entity.FinancialEntryType;
import com.factory.management.entity.FinancialRecord;
import com.factory.management.entity.FinancialRecordStatus;
import com.factory.management.entity.Role;
import com.factory.management.repository.FinancialRecordRepository;
import com.factory.management.security.AuthorizationScope;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class FinancialComparisonTool implements AiDataTool {
    private static final Set<Role> ALLOWED = Set.of(
            Role.ADMIN, Role.DIRECTOR, Role.FACTORY_MANAGER, Role.FINANCE
    );

    private final FinancialRecordRepository repository;
    private final AuthorizationScope authorizationScope;

    @Override
    public String name() {
        return "compare_financial_periods";
    }

    @Override
    public String description() {
        return "So sánh doanh thu, chi phí, lợi nhuận và công nợ giữa hai kỳ. "
                + "Chỉ dùng bản ghi tài chính active có trạng thái POSTED và đúng phạm vi tài khoản.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put(
                "currentFrom",
                AiToolArguments.nullableString("Ngày bắt đầu kỳ hiện tại ISO yyyy-MM-dd; null là đầu tháng hiện tại.")
        );
        properties.put("currentTo", AiToolArguments.nullableString("Ngày kết thúc kỳ hiện tại; null là hôm nay."));
        properties.put("previousFrom", AiToolArguments.nullableString("Ngày bắt đầu kỳ so sánh; null là đầu tháng trước."));
        properties.put("previousTo", AiToolArguments.nullableString("Ngày kết thúc kỳ so sánh; null lấy cùng số ngày của kỳ hiện tại."));
        properties.put("factory", AiToolArguments.nullableString("Mã hoặc tên nhà máy; null nếu không lọc."));
        properties.put("department", AiToolArguments.nullableString("Mã hoặc tên phòng ban; null nếu không lọc."));
        properties.put("productionLine", AiToolArguments.nullableString("Mã hoặc tên dây chuyền; null nếu không lọc."));
        return AiToolArguments.objectSchema(properties);
    }

    @Override
    public Set<Role> allowedRoles() {
        return ALLOWED;
    }

    @Override
    @Transactional(readOnly = true)
    public AiToolResult execute(Map<String, Object> arguments, AiToolContext context) {
        LocalDate currentFrom = AiToolArguments.date(
                arguments, "currentFrom", context.today().withDayOfMonth(1));
        LocalDate currentTo = AiToolArguments.date(arguments, "currentTo", context.today());
        AiToolArguments.validatePeriod(currentFrom, currentTo, 366);

        LocalDate defaultPreviousFrom = currentFrom.minusMonths(1).withDayOfMonth(1);
        long currentDays = ChronoUnit.DAYS.between(currentFrom, currentTo);
        LocalDate defaultPreviousTo = defaultPreviousFrom.plusDays(currentDays);
        LocalDate previousMonthEnd = defaultPreviousFrom.withDayOfMonth(defaultPreviousFrom.lengthOfMonth());
        if (defaultPreviousTo.isAfter(previousMonthEnd)) defaultPreviousTo = previousMonthEnd;
        LocalDate previousFrom = AiToolArguments.date(arguments, "previousFrom", defaultPreviousFrom);
        LocalDate previousTo = AiToolArguments.date(arguments, "previousTo", defaultPreviousTo);
        AiToolArguments.validatePeriod(previousFrom, previousTo, 366);

        String factory = AiToolArguments.text(arguments, "factory");
        String department = AiToolArguments.text(arguments, "department");
        String line = AiToolArguments.text(arguments, "productionLine");
        List<FinancialRecord> current = find(context, currentFrom, currentTo, factory, department, line);
        List<FinancialRecord> previous = find(context, previousFrom, previousTo, factory, department, line);

        Map<String, Object> currentSummary = summary(current);
        Map<String, Object> previousSummary = summary(previous);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("currentPeriod", Map.of("fromDate", currentFrom, "toDate", currentTo));
        data.put("previousPeriod", Map.of("fromDate", previousFrom, "toDate", previousTo));
        data.put("current", currentSummary);
        data.put("previous", previousSummary);
        data.put("changes", changes(currentSummary, previousSummary));

        List<String> warnings = new ArrayList<>();
        if (current.isEmpty() && previous.isEmpty()) {
            warnings.add("Không có bản ghi tài chính POSTED trong hai kỳ và phạm vi được phép xem.");
        }
        return new AiToolResult(
                name(),
                "OFFICIAL",
                data,
                List.of(new AiToolResult.Source(
                        "FINANCIAL_RECORD", "Bản ghi tài chính đã ghi sổ (POSTED)",
                        "OFFICIAL", current.size() + previous.size(), LocalDateTime.now())),
                warnings
        );
    }

    private List<FinancialRecord> find(
            AiToolContext context,
            LocalDate from,
            LocalDate to,
            String factory,
            String department,
            String line
    ) {
        return repository.findAll((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.isTrue(root.get("active")));
                    predicates.add(cb.equal(root.get("status"), FinancialRecordStatus.POSTED));
                    predicates.add(cb.between(root.get("recordDate"), from, to));
                    addSelector(predicates, cb, root.get("factory"), factory);
                    if (department != null) addSelector(predicates, cb, root.get("department"), department);
                    if (line != null) addSelector(predicates, cb, root.get("productionLine"), line);
                    return cb.and(predicates.toArray(Predicate[]::new));
                }).stream()
                .filter(value -> canAccess(context, value))
                .toList();
    }

    private boolean canAccess(AiToolContext context, FinancialRecord value) {
        Long factoryId = value.getFactory().getId();
        Long departmentId = value.getDepartment() == null ? null : value.getDepartment().getId();
        Long lineId = value.getProductionLine() == null ? null : value.getProductionLine().getId();
        return context.workspaceRole() == null
                ? authorizationScope.canAccessFinancialScope(factoryId, departmentId, lineId)
                : authorizationScope.canAccessFinancialScopeAsRole(
                        context.workspaceRole(), factoryId, departmentId, lineId);
    }

    private void addSelector(
            List<Predicate> predicates,
            CriteriaBuilder cb,
            Path<?> relation,
            String raw
    ) {
        if (raw == null || raw.isBlank()) return;
        String value = AiToolArguments.normalized(raw);
        var code = cb.lower(relation.get("code").as(String.class));
        var name = cb.lower(relation.get("name").as(String.class));
        predicates.add(cb.or(cb.equal(code, value), cb.equal(name, value),
                cb.like(code, "%" + value + "%"), cb.like(name, "%" + value + "%")));
    }

    private Map<String, Object> summary(List<FinancialRecord> values) {
        BigDecimal revenue = sum(values, FinancialEntryType.REVENUE);
        BigDecimal expense = sum(values, FinancialEntryType.EXPENSE);
        BigDecimal receivable = values.stream()
                .filter(value -> value.getCategory().getEntryType() == FinancialEntryType.REVENUE)
                .map(value -> value.getAmount().subtract(zero(value.getPaidAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal payable = values.stream()
                .filter(value -> value.getCategory().getEntryType() == FinancialEntryType.EXPENSE)
                .map(value -> value.getAmount().subtract(zero(value.getPaidAmount())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, BigDecimal> expenseByGroup = new LinkedHashMap<>();
        for (ExpenseGroup group : ExpenseGroup.values()) {
            BigDecimal amount = values.stream()
                    .filter(value -> value.getCategory().getEntryType() == FinancialEntryType.EXPENSE
                            && value.getCategory().getExpenseGroup() == group)
                    .map(FinancialRecord::getAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            expenseByGroup.put(group.name(), amount);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordCount", values.size());
        result.put("revenue", revenue);
        result.put("expense", expense);
        result.put("profit", revenue.subtract(expense));
        result.put("profitMarginPercent", percent(revenue.subtract(expense), revenue));
        result.put("accountsReceivable", receivable);
        result.put("accountsPayable", payable);
        result.put("expenseByGroup", expenseByGroup);
        return result;
    }

    private Map<String, Object> changes(Map<String, Object> current, Map<String, Object> previous) {
        Map<String, Object> result = new LinkedHashMap<>();
        addChange(result, "revenue", (BigDecimal) current.get("revenue"), (BigDecimal) previous.get("revenue"));
        addChange(result, "expense", (BigDecimal) current.get("expense"), (BigDecimal) previous.get("expense"));
        addChange(result, "profit", (BigDecimal) current.get("profit"), (BigDecimal) previous.get("profit"));
        addChange(result, "accountsReceivable", (BigDecimal) current.get("accountsReceivable"),
                (BigDecimal) previous.get("accountsReceivable"));
        addChange(result, "accountsPayable", (BigDecimal) current.get("accountsPayable"),
                (BigDecimal) previous.get("accountsPayable"));
        return result;
    }

    private void addChange(Map<String, Object> target, String key, BigDecimal current, BigDecimal previous) {
        Map<String, Object> value = new LinkedHashMap<>();
        value.put("absolute", current.subtract(previous));
        value.put("percent", previous.signum() == 0 ? null : percent(current.subtract(previous), previous.abs()));
        target.put(key, value);
    }

    private BigDecimal sum(List<FinancialRecord> values, FinancialEntryType type) {
        return values.stream().filter(value -> value.getCategory().getEntryType() == type)
                .map(FinancialRecord::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal percent(BigDecimal numerator, BigDecimal denominator) {
        if (denominator == null || denominator.signum() == 0) return BigDecimal.ZERO.setScale(2);
        return numerator.multiply(BigDecimal.valueOf(100))
                .divide(denominator, 2, RoundingMode.HALF_UP);
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
