package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.ProductionOrderRequest;
import com.factory.management.dto.request.ProductionOrderStatusRequest;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.dto.response.ProductionOrderResponse;
import com.factory.management.entity.Machine;
import com.factory.management.entity.ProductionOrder;
import com.factory.management.entity.ProductionOrderStatus;
import com.factory.management.entity.ProductionPlan;
import com.factory.management.entity.ProductionPlanStatus;
import com.factory.management.entity.Role;
import com.factory.management.entity.Shift;
import com.factory.management.entity.Team;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.repository.MachineRepository;
import com.factory.management.repository.ProductionOrderRepository;
import com.factory.management.repository.ProductionPlanRepository;
import com.factory.management.repository.ShiftRepository;
import com.factory.management.repository.TeamRepository;
import com.factory.management.security.AuthorizationScope;
import com.factory.management.service.Service.CurrentUserService;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProductionOrderService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final DateTimeFormatter NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ProductionOrderRepository orderRepository;
    private final ProductionPlanRepository planRepository;
    private final TeamRepository teamRepository;
    private final MachineRepository machineRepository;
    private final ShiftRepository shiftRepository;
    private final AuthorizationScope authorizationScope;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<ProductionOrderResponse> search(
            LocalDate fromDate,
            LocalDate toDate,
            Long productionPlanId,
            Long teamId,
            Long machineId,
            Long shiftId,
            ProductionOrderStatus status,
            Boolean active,
            int page,
            int size
    ) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new AppException(ErrorCode.PRODUCTION_ORDER_INVALID_PERIOD);
        }
        Set<Long> visibleTeamIds = authorizationScope.accessibleTeamIds();
        Specification<ProductionOrder> specification = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (!hasGlobalRead()) {
                if (visibleTeamIds.isEmpty()) return cb.disjunction();
                predicates.add(root.get("team").get("id").in(visibleTeamIds));
            }
            if (fromDate != null) predicates.add(cb.greaterThanOrEqualTo(root.get("scheduledEnd"), fromDate));
            if (toDate != null) predicates.add(cb.lessThanOrEqualTo(root.get("scheduledStart"), toDate));
            if (productionPlanId != null) {
                predicates.add(cb.equal(root.get("productionPlan").get("id"), productionPlanId));
            }
            if (teamId != null) predicates.add(cb.equal(root.get("team").get("id"), teamId));
            if (machineId != null) predicates.add(cb.equal(root.get("machine").get("id"), machineId));
            if (shiftId != null) predicates.add(cb.equal(root.get("shift").get("id"), shiftId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (active != null) predicates.add(cb.equal(root.get("active"), active));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        var result = orderRepository.findAll(specification,
                PageRequest.of(safePage(page), safeSize(size),
                        Sort.by(Sort.Direction.DESC, "scheduledStart").and(Sort.by(Sort.Direction.DESC, "id"))));
        return PageResponse.from(result, this::response);
    }

    @Transactional(readOnly = true)
    public ProductionOrderResponse get(Long id) {
        ProductionOrder value = find(id);
        requireTeamAccess(value.getTeam().getId());
        return response(value);
    }

    @Transactional
    public ProductionOrderResponse create(ProductionOrderRequest request) {
        ProductionPlan plan = approvedPlan(request.getProductionPlanId());
        Team team = activeTeam(request.getTeamId());
        requireTeamAccess(team.getId());
        Machine machine = activeMachine(request.getMachineId());
        Shift shift = activeShift(request.getShiftId());
        validateAssignment(plan, team, machine, request.getScheduledStart(), request.getScheduledEnd());
        validateAllocatedQuantity(plan, null, request.getPlannedQuantity());

        ProductionOrder value = orderRepository.save(ProductionOrder.builder()
                .orderNo(uniqueOrderNo())
                .productionPlan(plan)
                .team(team)
                .machine(machine)
                .shift(shift)
                .scheduledStart(request.getScheduledStart())
                .scheduledEnd(request.getScheduledEnd())
                .plannedQuantity(request.getPlannedQuantity())
                .status(ProductionOrderStatus.DRAFT)
                .note(trimToNull(request.getNote()))
                .active(true)
                .createdBy(currentUserService.user().getUsername())
                .build());
        auditService.record("CREATE", "PRODUCTION_ORDER", value.getId(),
                "{\"orderNo\":\"" + value.getOrderNo() + "\",\"planId\":" + plan.getId() + "}");
        return response(value);
    }

    @Transactional
    public ProductionOrderResponse update(Long id, ProductionOrderRequest request) {
        ProductionOrder value = find(id);
        requireTeamAccess(value.getTeam().getId());
        requireDraft(value);

        ProductionPlan plan = approvedPlan(request.getProductionPlanId());
        Team team = activeTeam(request.getTeamId());
        requireTeamAccess(team.getId());
        Machine machine = activeMachine(request.getMachineId());
        Shift shift = activeShift(request.getShiftId());
        validateAssignment(plan, team, machine, request.getScheduledStart(), request.getScheduledEnd());
        validateAllocatedQuantity(plan, id, request.getPlannedQuantity());

        value.setProductionPlan(plan);
        value.setTeam(team);
        value.setMachine(machine);
        value.setShift(shift);
        value.setScheduledStart(request.getScheduledStart());
        value.setScheduledEnd(request.getScheduledEnd());
        value.setPlannedQuantity(request.getPlannedQuantity());
        value.setNote(trimToNull(request.getNote()));
        auditService.record("UPDATE", "PRODUCTION_ORDER", value.getId(), null);
        return response(value);
    }

    @Transactional
    public ProductionOrderResponse changeStatus(Long id, ProductionOrderStatusRequest request) {
        ProductionOrder value = find(id);
        requireTeamAccess(value.getTeam().getId());
        ProductionOrderStatus oldStatus = value.getStatus();
        ProductionOrderStatus newStatus = request.getStatus();
        if (!validTransition(oldStatus, newStatus)) {
            throw new AppException(ErrorCode.INVALID_PRODUCTION_ORDER_TRANSITION);
        }
        if (newStatus != ProductionOrderStatus.CANCELLED) {
            approvedPlan(value.getProductionPlan().getId());
            validateCurrentAssignment(value);
        }
        value.setStatus(newStatus);
        LocalDateTime now = LocalDateTime.now();
        if (newStatus == ProductionOrderStatus.RELEASED) value.setReleasedAt(now);
        if (newStatus == ProductionOrderStatus.IN_PROGRESS) value.setStartedAt(now);
        if (newStatus == ProductionOrderStatus.COMPLETED) value.setCompletedAt(now);
        auditService.record("STATUS_CHANGE", "PRODUCTION_ORDER", value.getId(),
                "{\"from\":\"" + oldStatus + "\",\"to\":\"" + newStatus + "\"}");
        return response(value);
    }

    @Transactional
    public void delete(Long id) {
        ProductionOrder value = find(id);
        requireTeamAccess(value.getTeam().getId());
        requireDraft(value);
        value.setActive(false);
        auditService.record("SOFT_DELETE", "PRODUCTION_ORDER", value.getId(), null);
    }

    private ProductionOrder find(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_ORDER_NOT_FOUND));
    }

    private ProductionPlan approvedPlan(Long id) {
        ProductionPlan value = planRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_PLAN_NOT_FOUND));
        if (!Boolean.TRUE.equals(value.getActive()) || value.getStatus() != ProductionPlanStatus.APPROVED) {
            throw new AppException(ErrorCode.PRODUCTION_PLAN_NOT_APPROVED);
        }
        return value;
    }

    private Team activeTeam(Long id) {
        return teamRepository
                .findActiveByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_ID_NOT_FOUND));
    }

    private Machine activeMachine(Long id) {
        return machineRepository
                .findActiveByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_ID_NOT_FOUND));
    }

    private Shift activeShift(Long id) {
        return shiftRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_ID_NOT_FOUND));
    }

    private void validateAssignment(
            ProductionPlan plan,
            Team team,
            Machine machine,
            LocalDate scheduledStart,
            LocalDate scheduledEnd
    ) {
        if (scheduledStart.isAfter(scheduledEnd)
                || scheduledStart.isBefore(plan.getPeriodStart())
                || scheduledEnd.isAfter(plan.getPeriodEnd())) {
            throw new AppException(ErrorCode.PRODUCTION_ORDER_INVALID_PERIOD);
        }
        if (!team.getProductionLine().getId().equals(plan.getProductionLine().getId())
                || !machine.getTeam().getId().equals(team.getId())) {
            throw new AppException(ErrorCode.PRODUCTION_ORDER_INVALID_HIERARCHY);
        }
    }

    private void validateCurrentAssignment(ProductionOrder value) {
        if (!Boolean.TRUE.equals(value.getActive())
                || !Boolean.TRUE.equals(value.getTeam().getActive())
                || !Boolean.TRUE.equals(value.getMachine().getActive())
                || !Boolean.TRUE.equals(value.getShift().getActive())) {
            throw new AppException(ErrorCode.PRODUCTION_ORDER_INACTIVE_MASTER_DATA);
        }
        validateAssignment(value.getProductionPlan(), value.getTeam(), value.getMachine(),
                value.getScheduledStart(), value.getScheduledEnd());
    }

    private void validateAllocatedQuantity(ProductionPlan plan, Long excludeOrderId, BigDecimal quantity) {
        BigDecimal allocated = orderRepository.sumActivePlannedQuantity(plan.getId(), excludeOrderId);
        if (allocated == null) allocated = BigDecimal.ZERO;
        if (allocated.add(quantity).compareTo(plan.getPlannedQuantity()) > 0) {
            throw new AppException(ErrorCode.PRODUCTION_ORDER_TOTAL_EXCEEDS_PLAN);
        }
    }

    private void requireDraft(ProductionOrder value) {
        if (!Boolean.TRUE.equals(value.getActive()) || value.getStatus() != ProductionOrderStatus.DRAFT) {
            throw new AppException(ErrorCode.PRODUCTION_ORDER_NOT_DRAFT);
        }
    }

    private boolean validTransition(ProductionOrderStatus from, ProductionOrderStatus to) {
        if (from == to) return false;
        return switch (from) {
            case DRAFT -> to == ProductionOrderStatus.RELEASED || to == ProductionOrderStatus.CANCELLED;
            case RELEASED -> to == ProductionOrderStatus.IN_PROGRESS || to == ProductionOrderStatus.CANCELLED;
            case IN_PROGRESS -> to == ProductionOrderStatus.COMPLETED || to == ProductionOrderStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private void requireTeamAccess(Long teamId) {
        if (hasGlobalRead()) return;
        if (!authorizationScope.canAccessTeam(teamId)) throw new AppException(ErrorCode.ACCESS_DENIED);
    }

    private boolean hasGlobalRead() {
        Set<Role> roles = authorizationScope.currentRoles();
        return roles.contains(Role.ADMIN) || roles.contains(Role.DIRECTOR);
    }

    private String uniqueOrderNo() {
        String value;
        do {
            value = "PO" + LocalDate.now().format(NUMBER_DATE) + "-"
                    + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
        } while (orderRepository.existsByOrderNo(value));
        return value;
    }

    private ProductionOrderResponse response(ProductionOrder value) {
        ProductionPlan plan = value.getProductionPlan();
        return ProductionOrderResponse.builder()
                .id(value.getId())
                .orderNo(value.getOrderNo())
                .productionPlanId(plan.getId())
                .productionPlanNo(plan.getPlanNo())
                .productId(plan.getProduct().getId())
                .productCode(plan.getProduct().getCode())
                .productName(plan.getProduct().getName())
                .teamId(value.getTeam().getId())
                .teamCode(value.getTeam().getCode())
                .teamName(value.getTeam().getName())
                .machineId(value.getMachine().getId())
                .machineCode(value.getMachine().getCode())
                .machineName(value.getMachine().getName())
                .shiftId(value.getShift().getId())
                .shiftCode(value.getShift().getCode())
                .shiftName(value.getShift().getName())
                .scheduledStart(value.getScheduledStart())
                .scheduledEnd(value.getScheduledEnd())
                .plannedQuantity(value.getPlannedQuantity())
                .status(value.getStatus())
                .note(value.getNote())
                .active(value.getActive())
                .releasedAt(value.getReleasedAt())
                .startedAt(value.getStartedAt())
                .completedAt(value.getCompletedAt())
                .createdBy(value.getCreatedBy())
                .createdAt(value.getCreatedAt())
                .updatedAt(value.getUpdatedAt())
                .version(value.getVersion())
                .build();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private int safePage(int value) {
        return Math.max(value, 0);
    }

    private int safeSize(int value) {
        return Math.min(Math.max(value, 1), MAX_PAGE_SIZE);
    }
}
