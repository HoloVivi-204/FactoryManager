package com.factory.management.modules.ai.tool;

import com.factory.management.modules.ai.AiDataTool;
import com.factory.management.modules.ai.AiToolArguments;
import com.factory.management.modules.ai.AiToolContext;
import com.factory.management.modules.ai.AiToolResult;
import com.factory.management.modules.maintenance.entity.MaintenancePartUsage;
import com.factory.management.modules.maintenance.entity.MaintenanceWorkOrder;
import com.factory.management.modules.maintenance.entity.MaintenanceWorkOrderStatus;
import com.factory.management.modules.auth.entity.Role;
import com.factory.management.modules.maintenance.repository.MaintenancePartUsageRepository;
import com.factory.management.modules.maintenance.repository.MaintenanceWorkOrderRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MaintenanceCostRankingTool implements AiDataTool {
    private static final Set<Role> ALLOWED = Set.of(
            Role.ADMIN, Role.DIRECTOR, Role.FACTORY_MANAGER,
            Role.PRODUCTION_MANAGER, Role.FINANCE
    );

    private final MaintenanceWorkOrderRepository workOrderRepository;
    private final MaintenancePartUsageRepository partUsageRepository;

    @Override
    public String name() {
        return "rank_maintenance_cost";
    }

    @Override
    public String description() {
        return "Xếp hạng máy theo chi phí bảo trì đã hoàn thành trong một kỳ, gồm nhân công, vật tư và thuê ngoài. "
                + "Chỉ dùng phiếu bảo trì COMPLETED và máy thuộc phạm vi được xem.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("fromDate", AiToolArguments.nullableString("Ngày bắt đầu ISO yyyy-MM-dd; null là đầu quý hiện tại."));
        properties.put("toDate", AiToolArguments.nullableString("Ngày kết thúc ISO yyyy-MM-dd; null là hôm nay."));
        properties.put("factory", AiToolArguments.nullableString("Mã hoặc tên nhà máy; null nếu không lọc."));
        properties.put("department", AiToolArguments.nullableString("Mã hoặc tên phòng ban; null nếu không lọc."));
        properties.put("productionLine", AiToolArguments.nullableString("Mã hoặc tên dây chuyền; null nếu không lọc."));
        properties.put("team", AiToolArguments.nullableString("Mã hoặc tên tổ; null nếu không lọc."));
        properties.put("limit", AiToolArguments.nullableInteger("Số máy tối đa, từ 1 đến 20; null là 10.", 1, 20));
        return AiToolArguments.objectSchema(properties);
    }

    @Override
    public Set<Role> allowedRoles() {
        return ALLOWED;
    }

    @Override
    @Transactional(readOnly = true)
    public AiToolResult execute(Map<String, Object> arguments, AiToolContext context) {
        LocalDate quarterStart = quarterStart(context.today());
        LocalDate from = AiToolArguments.date(arguments, "fromDate", quarterStart);
        LocalDate to = AiToolArguments.date(arguments, "toDate", context.today());
        AiToolArguments.validatePeriod(from, to, 366);
        int limit = AiToolArguments.integer(arguments, "limit", 10, 1, 20);

        List<MaintenanceWorkOrder> workOrders = find(
                context,
                from.atStartOfDay(),
                to.plusDays(1).atStartOfDay(),
                AiToolArguments.text(arguments, "factory"),
                AiToolArguments.text(arguments, "department"),
                AiToolArguments.text(arguments, "productionLine"),
                AiToolArguments.text(arguments, "team")
        );
        Map<Long, BigDecimal> partCosts = partCosts(workOrders);
        List<Map<String, Object>> ranking = workOrders.stream()
                .collect(Collectors.groupingBy(
                        value -> value.getMachine().getId(), LinkedHashMap::new, Collectors.toList()))
                .values().stream()
                .map(values -> machineCost(values, partCosts))
                .sorted(Comparator.comparing((Map<String, Object> value) ->
                        (BigDecimal) value.get("totalCost")).reversed())
                .limit(limit)
                .toList();

        BigDecimal total = ranking.stream().map(value -> (BigDecimal) value.get("totalCost"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("period", Map.of("fromDate", from, "toDate", to));
        data.put("completedWorkOrderCount", workOrders.size());
        data.put("rankedMachineCount", ranking.size());
        data.put("rankedTotalCost", total);
        data.put("machines", ranking);

        List<String> warnings = new ArrayList<>();
        if (context.accessibleTeamIds().isEmpty()) {
            warnings.add("Tài khoản chưa được cấp phạm vi máy/tổ để xem chi phí bảo trì.");
        }
        if (workOrders.isEmpty()) {
            warnings.add("Không có phiếu bảo trì COMPLETED trong kỳ và phạm vi được phép xem.");
        }
        return new AiToolResult(
                name(), "OFFICIAL", data,
                List.of(new AiToolResult.Source(
                        "MAINTENANCE_WORK_ORDER", "Phiếu bảo trì đã hoàn thành",
                        "OFFICIAL", workOrders.size(), LocalDateTime.now())),
                warnings
        );
    }

    private List<MaintenanceWorkOrder> find(
            AiToolContext context,
            LocalDateTime from,
            LocalDateTime toExclusive,
            String factory,
            String department,
            String line,
            String team
    ) {
        if (context.accessibleTeamIds().isEmpty()) return List.of();
        return workOrderRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.equal(root.get("status"), MaintenanceWorkOrderStatus.COMPLETED));
            predicates.add(cb.isNotNull(root.get("actualEnd")));
            predicates.add(cb.greaterThanOrEqualTo(root.get("actualEnd"), from));
            predicates.add(cb.lessThan(root.get("actualEnd"), toExclusive));
            var machine = root.get("machine");
            var teamPath = machine.get("team");
            predicates.add(teamPath.get("id").in(context.accessibleTeamIds()));
            addSelector(predicates, cb, teamPath, team);
            var linePath = teamPath.get("productionLine");
            addSelector(predicates, cb, linePath, line);
            var departmentPath = linePath.get("department");
            addSelector(predicates, cb, departmentPath, department);
            addSelector(predicates, cb, departmentPath.get("factory"), factory);
            return cb.and(predicates.toArray(Predicate[]::new));
        });
    }

    private Map<Long, BigDecimal> partCosts(List<MaintenanceWorkOrder> workOrders) {
        if (workOrders.isEmpty()) return Map.of();
        Set<Long> ids = workOrders.stream().map(MaintenanceWorkOrder::getId).collect(Collectors.toSet());
        Map<Long, BigDecimal> result = new HashMap<>();
        for (MaintenancePartUsage part : partUsageRepository.findAllByWorkOrder_IdInAndActiveTrue(ids)) {
            result.merge(part.getWorkOrder().getId(),
                    part.getQuantity().multiply(part.getUnitCost()), BigDecimal::add);
        }
        return result;
    }

    private Map<String, Object> machineCost(
            List<MaintenanceWorkOrder> workOrders,
            Map<Long, BigDecimal> partCosts
    ) {
        MaintenanceWorkOrder first = workOrders.get(0);
        BigDecimal labor = workOrders.stream().map(value -> zero(value.getLaborCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal external = workOrders.stream().map(value -> zero(value.getExternalCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal parts = workOrders.stream().map(value -> partCosts.getOrDefault(value.getId(), BigDecimal.ZERO))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("machineId", first.getMachine().getId());
        result.put("machineCode", first.getMachine().getCode());
        result.put("machineName", first.getMachine().getName());
        result.put("teamId", first.getMachine().getTeam().getId());
        result.put("teamName", first.getMachine().getTeam().getName());
        result.put("workOrderCount", workOrders.size());
        result.put("laborCost", labor);
        result.put("partCost", parts);
        result.put("externalCost", external);
        result.put("totalCost", labor.add(parts).add(external));
        return result;
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

    private LocalDate quarterStart(LocalDate date) {
        int firstMonth = ((date.getMonthValue() - 1) / 3) * 3 + 1;
        return LocalDate.of(date.getYear(), Month.of(firstMonth), 1);
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
