package com.factory.management.modules.ai.tool;

import com.factory.management.modules.ai.AiToolContext;
import com.factory.management.modules.production.entity.ProductionReport;
import com.factory.management.modules.production.repository.ProductionReportRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class ScopedProductionQuery {
    private final ProductionReportRepository repository;

    @Transactional(readOnly = true)
    public List<ProductionReport> find(
            AiToolContext context,
            LocalDate from,
            LocalDate to,
            String factory,
            String department,
            String productionLine,
            String team,
            String machine
    ) {
        if (context.accessibleTeamIds().isEmpty()) return List.of();
        return repository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.between(root.get("reportDate"), from, to));
            predicates.add(root.get("team").get("id").in(context.accessibleTeamIds()));
            addSelector(predicates, cb, root.get("factory"), factory);
            addSelector(predicates, cb, root.get("department"), department);
            addSelector(predicates, cb, root.get("productionLine"), productionLine);
            addSelector(predicates, cb, root.get("team"), team);
            addSelector(predicates, cb, root.get("machine"), machine);
            query.orderBy(cb.asc(root.get("reportDate")), cb.asc(root.get("id")));
            return cb.and(predicates.toArray(Predicate[]::new));
        });
    }

    private void addSelector(
            List<Predicate> predicates,
            CriteriaBuilder cb,
            Path<?> relation,
            String raw
    ) {
        if (raw == null || raw.isBlank()) return;
        String value = raw.trim().toLowerCase(java.util.Locale.ROOT);
        var code = cb.lower(relation.get("code").as(String.class));
        var name = cb.lower(relation.get("name").as(String.class));
        predicates.add(cb.or(
                cb.equal(code, value),
                cb.equal(name, value),
                cb.like(code, "%" + value + "%"),
                cb.like(name, "%" + value + "%")
        ));
    }
}
