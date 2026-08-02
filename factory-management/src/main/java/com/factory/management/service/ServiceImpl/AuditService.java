package com.factory.management.service.ServiceImpl;

import com.factory.management.config.RequestCorrelationFilter;
import com.factory.management.dto.response.AuditEventResponse;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.entity.AuditEvent;
import com.factory.management.entity.User;
import com.factory.management.repository.AuditEventRepository;
import com.factory.management.repository.UserRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Service
@RequiredArgsConstructor
public class AuditService {

    private static final int MAX_PAGE_SIZE = 200;

    private final AuditEventRepository repository;
    private final UserRepository userRepository;

    /**
     * Participates in the caller transaction so an event is never committed for
     * a business mutation that is rolled back.
     */
    @Transactional
    public void record(String action, String entityType, Object entityId, String detailsJson) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication == null || !authentication.isAuthenticated()
                ? "SYSTEM"
                : authentication.getName();
        Long employeeId = userRepository.findByUsernameIgnoreCase(username)
                .map(User::getEmployee)
                .map(employee -> employee.getId())
                .orElse(null);

        HttpServletRequest request = currentRequest();
        repository.save(AuditEvent.builder()
                .actorUsername(username)
                .actorEmployeeId(employeeId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId == null ? null : String.valueOf(entityId))
                .requestId(MDC.get(RequestCorrelationFilter.REQUEST_ID_MDC_KEY))
                .ipAddress(request == null ? null : clientIp(request))
                .detailsJson(detailsJson)
                .occurredAt(LocalDateTime.now())
                .build());
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditEventResponse> search(
            LocalDateTime from,
            LocalDateTime to,
            String actor,
            String action,
            String entityType,
            int page,
            int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        var specification = (org.springframework.data.jpa.domain.Specification<AuditEvent>) (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("occurredAt"), from));
            if (to != null) predicates.add(cb.lessThanOrEqualTo(root.get("occurredAt"), to));
            if (actor != null && !actor.isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("actorUsername")), actor.trim().toLowerCase()));
            }
            if (action != null && !action.isBlank()) predicates.add(cb.equal(root.get("action"), action.trim()));
            if (entityType != null && !entityType.isBlank()) {
                predicates.add(cb.equal(root.get("entityType"), entityType.trim()));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        var result = repository.findAll(specification,
                PageRequest.of(safePage, safeSize, Sort.by(Sort.Direction.DESC, "occurredAt")));
        return PageResponse.from(result, this::response);
    }

    private AuditEventResponse response(AuditEvent value) {
        return AuditEventResponse.builder()
                .id(value.getId())
                .actorUsername(value.getActorUsername())
                .actorEmployeeId(value.getActorEmployeeId())
                .action(value.getAction())
                .entityType(value.getEntityType())
                .entityId(value.getEntityId())
                .requestId(value.getRequestId())
                .ipAddress(value.getIpAddress())
                .detailsJson(value.getDetailsJson())
                .occurredAt(value.getOccurredAt())
                .build();
    }

    private HttpServletRequest currentRequest() {
        var attributes = RequestContextHolder.getRequestAttributes();
        return attributes instanceof ServletRequestAttributes servlet ? servlet.getRequest() : null;
    }

    private String clientIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return forwarded == null || forwarded.isBlank()
                ? request.getRemoteAddr()
                : forwarded.split(",", 2)[0].trim();
    }
}
