package com.factory.management.modules.maintenance.service;

import com.factory.management.common.api.PageResponse;
import com.factory.management.common.error.AppException;
import com.factory.management.common.error.ErrorCode;
import com.factory.management.common.security.AuthorizationScope;
import com.factory.management.common.security.CurrentUserService;
import com.factory.management.modules.audit.service.AuditService;
import com.factory.management.modules.maintenance.dto.request.MaintenancePartUsageRequest;
import com.factory.management.modules.maintenance.dto.request.MaintenanceRequestCreateRequest;
import com.factory.management.modules.maintenance.dto.request.MaintenanceRequestStatusRequest;
import com.factory.management.modules.maintenance.dto.request.MaintenanceScheduleRequest;
import com.factory.management.modules.maintenance.dto.request.MaintenanceWorkOrderRequest;
import com.factory.management.modules.maintenance.dto.request.MaintenanceWorkOrderStatusRequest;
import com.factory.management.modules.maintenance.dto.response.MaintenanceResponse;
import com.factory.management.modules.maintenance.entity.MachineOperationalStatus;
import com.factory.management.modules.maintenance.entity.MachineStatusHistory;
import com.factory.management.modules.maintenance.entity.MaintenancePartUsage;
import com.factory.management.modules.maintenance.entity.MaintenancePriority;
import com.factory.management.modules.maintenance.entity.MaintenanceRequest;
import com.factory.management.modules.maintenance.entity.MaintenanceRequestStatus;
import com.factory.management.modules.maintenance.entity.MaintenanceSchedule;
import com.factory.management.modules.maintenance.entity.MaintenanceType;
import com.factory.management.modules.maintenance.entity.MaintenanceWorkOrder;
import com.factory.management.modules.maintenance.entity.MaintenanceWorkOrderStatus;
import com.factory.management.modules.maintenance.repository.MachineStatusHistoryRepository;
import com.factory.management.modules.maintenance.repository.MaintenancePartUsageRepository;
import com.factory.management.modules.maintenance.repository.MaintenanceRequestRepository;
import com.factory.management.modules.maintenance.repository.MaintenanceScheduleRepository;
import com.factory.management.modules.maintenance.repository.MaintenanceWorkOrderRepository;
import com.factory.management.modules.masterdata.entity.Employee;
import com.factory.management.modules.masterdata.entity.Machine;
import com.factory.management.modules.masterdata.entity.Material;
import com.factory.management.modules.masterdata.repository.EmployeeRepository;
import com.factory.management.modules.masterdata.repository.MachineRepository;
import com.factory.management.modules.masterdata.repository.MaterialRepository;
import com.factory.management.modules.production.entity.MachineDowntimeStaging;
import com.factory.management.modules.production.repository.MachineDowntimeStagingRepository;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MaintenanceService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final DateTimeFormatter NUMBER_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final MaintenanceRequestRepository requestRepository;
    private final MaintenanceScheduleRepository scheduleRepository;
    private final MaintenanceWorkOrderRepository workOrderRepository;
    private final MaintenancePartUsageRepository partUsageRepository;
    private final MachineStatusHistoryRepository statusHistoryRepository;
    private final MachineRepository machineRepository;
    private final EmployeeRepository employeeRepository;
    private final MaterialRepository materialRepository;
    private final MachineDowntimeStagingRepository downtimeRepository;
    private final AuthorizationScope authorizationScope;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<MaintenanceResponse.MachineOption> machineOptions() {
        Set<Long> visibleTeamIds = authorizationScope.accessibleTeamIds();
        return machineRepository
                .findAllActiveInActiveHierarchy()
                .stream()
                .filter(machine -> visibleTeamIds.contains(machine.getTeam().getId()))
                .sorted(java.util.Comparator.comparing(Machine::getCode, String.CASE_INSENSITIVE_ORDER))
                .map(machine -> MaintenanceResponse.MachineOption.builder()
                        .id(machine.getId())
                        .code(machine.getCode())
                        .name(machine.getName())
                        .teamId(machine.getTeam().getId())
                        .teamName(machine.getTeam().getName())
                        .operationalStatus(machine.getOperationalStatus())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<MaintenanceResponse.RequestItem> requests(
            Long machineId,
            Long teamId,
            MaintenanceRequestStatus status,
            MaintenancePriority priority,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate,
            int page,
            int size
    ) {
        var result = requestRepository.findAll(
                requestSpec(machineId, teamId, status, priority, keyword, fromDate, toDate),
                page(page, size, "reportedAt"));
        return PageResponse.from(result, this::requestItem);
    }

    @Transactional(readOnly = true)
    public MaintenanceResponse.RequestItem request(Long id) {
        MaintenanceRequest value = requestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MAINTENANCE_REQUEST_NOT_FOUND));
        requireMachineAccess(value.getMachine().getId());
        return requestItem(value);
    }

    @Transactional
    public MaintenanceResponse.RequestItem createRequest(MaintenanceRequestCreateRequest input) {
        Machine machine = activeMachine(input.getMachineId());
        requireMachineAccess(machine.getId());
        Employee reporter = currentUserService.employee();
        MachineDowntimeStaging source = sourceDowntime(input.getSourceDowntimeStagingId(), machine);

        MaintenanceRequest value = requestRepository.save(MaintenanceRequest.builder()
                .requestNo(uniqueNumber("MR"))
                .machine(machine)
                .reportedBy(reporter)
                .sourceDowntimeStaging(source)
                .priority(input.getPriority())
                .status(MaintenanceRequestStatus.OPEN)
                .title(input.getTitle().trim())
                .description(input.getDescription().trim())
                .impactDescription(trimToNull(input.getImpactDescription()))
                .active(true)
                .build());
        auditService.record("CREATE", "MAINTENANCE_REQUEST", value.getId(),
                "{\"requestNo\":\"" + value.getRequestNo() + "\",\"machineId\":" + machine.getId() + "}");
        return requestItem(value);
    }

    @Transactional
    public MaintenanceResponse.RequestItem updateRequestStatus(Long id, MaintenanceRequestStatusRequest input) {
        MaintenanceRequest value = requestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MAINTENANCE_REQUEST_NOT_FOUND));
        requireMachineAccess(value.getMachine().getId());
        MaintenanceRequestStatus old = value.getStatus();
        if (!validRequestTransition(old, input.getStatus())) {
            throw new AppException(ErrorCode.INVALID_MAINTENANCE_REQUEST_TRANSITION);
        }
        if (input.getStatus() == MaintenanceRequestStatus.RESOLVED
                && trimToNull(input.getResolutionNote()) == null) {
            throw new AppException(ErrorCode.MAINTENANCE_RESOLUTION_REQUIRED);
        }
        value.setStatus(input.getStatus());
        value.setResolutionNote(trimToNull(input.getResolutionNote()));
        value.setResolvedAt(input.getStatus() == MaintenanceRequestStatus.RESOLVED ? LocalDateTime.now() : null);
        auditService.record("STATUS_CHANGE", "MAINTENANCE_REQUEST", value.getId(),
                "{\"from\":\"" + old + "\",\"to\":\"" + input.getStatus() + "\"}");
        return requestItem(value);
    }

    @Transactional(readOnly = true)
    public PageResponse<MaintenanceResponse.ScheduleItem> schedules(
            Long machineId,
            Long teamId,
            Boolean active,
            LocalDate dueBefore,
            int page,
            int size
    ) {
        var result = scheduleRepository.findAll(scheduleSpec(machineId, teamId, active, dueBefore),
                page(page, size, "nextDueDate"));
        return PageResponse.from(result, this::scheduleItem);
    }

    @Transactional(readOnly = true)
    public MaintenanceResponse.ScheduleItem schedule(Long id) {
        MaintenanceSchedule value = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MAINTENANCE_SCHEDULE_NOT_FOUND));
        requireMachineAccess(value.getMachine().getId());
        return scheduleItem(value);
    }

    @Transactional
    public MaintenanceResponse.ScheduleItem saveSchedule(Long id, MaintenanceScheduleRequest input) {
        Machine machine = activeMachine(input.getMachineId());
        requireMachineAccess(machine.getId());
        validateScheduleDates(input);

        MaintenanceSchedule value;
        String action;
        if (id == null) {
            value = MaintenanceSchedule.builder().createdBy(currentUserService.user().getUsername()).build();
            action = "CREATE";
        } else {
            value = scheduleRepository.findById(id)
                    .orElseThrow(() -> new AppException(ErrorCode.MAINTENANCE_SCHEDULE_NOT_FOUND));
            requireMachineAccess(value.getMachine().getId());
            action = "UPDATE";
        }
        value.setMachine(machine);
        value.setMaintenanceType(input.getMaintenanceType());
        value.setName(input.getName().trim());
        value.setIntervalDays(input.getIntervalDays());
        value.setLastCompletedDate(input.getLastCompletedDate());
        value.setNextDueDate(input.getNextDueDate());
        value.setDescription(trimToNull(input.getDescription()));
        if (input.getActive() != null) value.setActive(input.getActive());
        if (value.getActive() == null) value.setActive(true);
        value = scheduleRepository.save(value);
        auditService.record(action, "MAINTENANCE_SCHEDULE", value.getId(),
                "{\"machineId\":" + machine.getId() + "}");
        return scheduleItem(value);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        MaintenanceSchedule value = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MAINTENANCE_SCHEDULE_NOT_FOUND));
        requireMachineAccess(value.getMachine().getId());
        value.setActive(false);
        auditService.record("DEACTIVATE", "MAINTENANCE_SCHEDULE", value.getId(), null);
    }

    @Transactional(readOnly = true)
    public PageResponse<MaintenanceResponse.WorkOrderItem> workOrders(
            Long machineId,
            Long teamId,
            MaintenanceWorkOrderStatus status,
            MaintenancePriority priority,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {
        if (from != null && to != null && to.isBefore(from)) {
            throw new AppException(ErrorCode.INVALID_MAINTENANCE_DATE_RANGE);
        }
        var result = workOrderRepository.findAll(
                workOrderSpec(machineId, teamId, status, priority, from, to),
                page(page, size, "plannedStart"));
        return PageResponse.from(result, this::workOrderItem);
    }

    @Transactional(readOnly = true)
    public MaintenanceResponse.WorkOrderItem workOrder(Long id) {
        MaintenanceWorkOrder value = workOrderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MAINTENANCE_WORK_ORDER_NOT_FOUND));
        requireMachineAccess(value.getMachine().getId());
        return workOrderItem(value);
    }

    @Transactional
    public MaintenanceResponse.WorkOrderItem createWorkOrder(MaintenanceWorkOrderRequest input) {
        validateWorkOrderDates(input.getPlannedStart(), input.getPlannedEnd());
        Machine machine = activeMachine(input.getMachineId());
        requireMachineAccess(machine.getId());
        MaintenanceRequest maintenanceRequest = linkedRequest(input.getMaintenanceRequestId(), machine);
        MaintenanceSchedule maintenanceSchedule = linkedSchedule(input.getMaintenanceScheduleId(), machine);
        if (maintenanceRequest == null && maintenanceSchedule == null) {
            throw new AppException(ErrorCode.MAINTENANCE_WORK_ORDER_SOURCE_REQUIRED);
        }
        Employee assignee = assignedEmployee(input.getAssignedEmployeeId());

        MaintenanceWorkOrder value = workOrderRepository.save(MaintenanceWorkOrder.builder()
                .workOrderNo(uniqueNumber("MWO"))
                .maintenanceRequest(maintenanceRequest)
                .maintenanceSchedule(maintenanceSchedule)
                .machine(machine)
                .maintenanceType(input.getMaintenanceType())
                .priority(input.getPriority())
                .status(assignee == null ? MaintenanceWorkOrderStatus.PLANNED : MaintenanceWorkOrderStatus.ASSIGNED)
                .assignedEmployee(assignee)
                .title(input.getTitle().trim())
                .description(trimToNull(input.getDescription()))
                .plannedStart(input.getPlannedStart())
                .plannedEnd(input.getPlannedEnd())
                .laborCost(zero(input.getLaborCost()))
                .externalCost(zero(input.getExternalCost()))
                .createdBy(currentUserService.user().getUsername())
                .build());
        if (maintenanceRequest != null && maintenanceRequest.getStatus() == MaintenanceRequestStatus.OPEN) {
            maintenanceRequest.setStatus(MaintenanceRequestStatus.ACKNOWLEDGED);
        }
        auditService.record("CREATE", "MAINTENANCE_WORK_ORDER", value.getId(),
                "{\"workOrderNo\":\"" + value.getWorkOrderNo() + "\",\"machineId\":" + machine.getId() + "}");
        return workOrderItem(value);
    }

    @Transactional
    public MaintenanceResponse.WorkOrderItem updateWorkOrderStatus(
            Long id,
            MaintenanceWorkOrderStatusRequest input
    ) {
        MaintenanceWorkOrder value = workOrderRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MAINTENANCE_WORK_ORDER_NOT_FOUND));
        requireMachineAccess(value.getMachine().getId());
        MaintenanceWorkOrderStatus old = value.getStatus();
        if (!validWorkOrderTransition(old, input.getStatus())) {
            throw new AppException(ErrorCode.INVALID_MAINTENANCE_WORK_ORDER_TRANSITION);
        }

        if (input.getStatus() == MaintenanceWorkOrderStatus.IN_PROGRESS) {
            value.setActualStart(input.getActualStart() == null ? LocalDateTime.now() : input.getActualStart());
            changeMachineStatus(value.getMachine(), MachineOperationalStatus.MAINTENANCE,
                    "MAINTENANCE_WORK_ORDER", value.getId(), "Bắt đầu bảo trì: " + value.getWorkOrderNo());
            if (value.getMaintenanceRequest() != null
                    && !terminal(value.getMaintenanceRequest().getStatus())) {
                value.getMaintenanceRequest().setStatus(MaintenanceRequestStatus.IN_PROGRESS);
            }
        }

        if (input.getStatus() == MaintenanceWorkOrderStatus.COMPLETED) {
            LocalDateTime actualEnd = input.getActualEnd() == null ? LocalDateTime.now() : input.getActualEnd();
            LocalDateTime actualStart = value.getActualStart() == null ? input.getActualStart() : value.getActualStart();
            if (actualStart == null || !actualEnd.isAfter(actualStart)) {
                throw new AppException(ErrorCode.INVALID_MAINTENANCE_ACTUAL_TIME_RANGE);
            }
            if (trimToNull(input.getCompletionNote()) == null) {
                throw new AppException(ErrorCode.MAINTENANCE_COMPLETION_NOTE_REQUIRED);
            }
            value.setActualStart(actualStart);
            value.setActualEnd(actualEnd);
            value.setCompletionNote(input.getCompletionNote().trim());
            completeLinkedRecords(value, actualEnd);
            boolean anotherRunning = workOrderRepository.existsByMachine_IdAndStatusAndIdNot(
                    value.getMachine().getId(), MaintenanceWorkOrderStatus.IN_PROGRESS, value.getId());
            if (!anotherRunning && value.getMachine().getOperationalStatus() == MachineOperationalStatus.MAINTENANCE) {
                changeMachineStatus(value.getMachine(), MachineOperationalStatus.IDLE,
                        "MAINTENANCE_WORK_ORDER", value.getId(), "Hoàn thành bảo trì: " + value.getWorkOrderNo());
            }
        }
        if (input.getStatus() == MaintenanceWorkOrderStatus.CANCELLED) {
            value.setCompletionNote(trimToNull(input.getCompletionNote()));
        }
        value.setStatus(input.getStatus());
        auditService.record("STATUS_CHANGE", "MAINTENANCE_WORK_ORDER", value.getId(),
                "{\"from\":\"" + old + "\",\"to\":\"" + input.getStatus() + "\"}");
        return workOrderItem(value);
    }

    @Transactional
    public MaintenanceResponse.PartUsageItem addPart(Long workOrderId, MaintenancePartUsageRequest input) {
        MaintenanceWorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.MAINTENANCE_WORK_ORDER_NOT_FOUND));
        requireMachineAccess(workOrder.getMachine().getId());
        requireMutableWorkOrder(workOrder);
        Material material = materialRepository.findByIdAndActiveTrue(input.getMaterialId())
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_ID_NOT_FOUND));
        if (partUsageRepository.existsByWorkOrder_IdAndMaterial_IdAndActiveTrue(workOrderId, material.getId())) {
            throw new AppException(ErrorCode.MAINTENANCE_PART_EXISTS);
        }
        MaintenancePartUsage usage = partUsageRepository.save(MaintenancePartUsage.builder()
                .workOrder(workOrder)
                .material(material)
                .quantity(input.getQuantity())
                .unit(material.getUnit())
                .unitCost(input.getUnitCost())
                .active(true)
                .build());
        auditService.record("ADD_PART", "MAINTENANCE_WORK_ORDER", workOrderId,
                "{\"materialId\":" + material.getId() + ",\"quantity\":" + input.getQuantity() + "}");
        return partItem(usage);
    }

    @Transactional
    public void deletePart(Long workOrderId, Long partId) {
        MaintenanceWorkOrder workOrder = workOrderRepository.findById(workOrderId)
                .orElseThrow(() -> new AppException(ErrorCode.MAINTENANCE_WORK_ORDER_NOT_FOUND));
        requireMachineAccess(workOrder.getMachine().getId());
        requireMutableWorkOrder(workOrder);
        MaintenancePartUsage usage = partUsageRepository.findById(partId)
                .filter(value -> value.getWorkOrder().getId().equals(workOrderId) && Boolean.TRUE.equals(value.getActive()))
                .orElseThrow(() -> new AppException(ErrorCode.MAINTENANCE_PART_NOT_FOUND));
        usage.setActive(false);
        auditService.record("REMOVE_PART", "MAINTENANCE_WORK_ORDER", workOrderId,
                "{\"partUsageId\":" + usage.getId() + "}");
    }

    @Transactional(readOnly = true)
    public PageResponse<MaintenanceResponse.StatusHistoryItem> machineHistory(Long machineId, int page, int size) {
        activeMachine(machineId);
        requireMachineAccess(machineId);
        var result = statusHistoryRepository.findAllByMachine_Id(machineId,
                page(page, size, "changedAt"));
        return PageResponse.from(result, this::historyItem);
    }

    @Transactional(readOnly = true)
    public MaintenanceResponse.Dashboard dashboard(Long teamId, LocalDate fromDate, LocalDate toDate) {
        if (fromDate != null && toDate != null && toDate.isBefore(fromDate)) {
            throw new AppException(ErrorCode.INVALID_MAINTENANCE_DATE_RANGE);
        }
        LocalDateTime from = fromDate == null ? null : fromDate.atStartOfDay();
        LocalDateTime to = toDate == null ? null : toDate.plusDays(1).atStartOfDay();
        List<MaintenanceRequest> requests = requestRepository.findAll(
                requestSpec(null, teamId, null, null, null, fromDate, toDate));
        List<MaintenanceSchedule> schedules = scheduleRepository.findAll(
                scheduleSpec(null, teamId, true, LocalDate.now().minusDays(1)));
        List<MaintenanceWorkOrder> workOrders = workOrderRepository.findAll(
                workOrderSpec(null, teamId, null, null, from, to));
        long open = requests.stream().filter(value -> !terminal(value.getStatus())).count();
        long critical = requests.stream().filter(value -> !terminal(value.getStatus())
                && value.getPriority() == MaintenancePriority.CRITICAL).count();
        long active = workOrders.stream().filter(value -> EnumSet.of(
                MaintenanceWorkOrderStatus.ASSIGNED,
                MaintenanceWorkOrderStatus.IN_PROGRESS).contains(value.getStatus())).count();
        BigDecimal cost = workOrders.stream()
                .filter(value -> value.getStatus() == MaintenanceWorkOrderStatus.COMPLETED)
                .map(this::totalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return MaintenanceResponse.Dashboard.builder()
                .openRequests(open)
                .criticalRequests(critical)
                .overdueSchedules(schedules.size())
                .activeWorkOrders(active)
                .completedCost(cost)
                .build();
    }

    private Specification<MaintenanceRequest> requestSpec(
            Long machineId,
            Long teamId,
            MaintenanceRequestStatus status,
            MaintenancePriority priority,
            String keyword,
            LocalDate fromDate,
            LocalDate toDate
    ) {
        Set<Long> teams = visibleTeams(teamId);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("machine").get("team").get("id").in(teams));
            predicates.add(cb.isTrue(root.get("active")));
            if (machineId != null) predicates.add(cb.equal(root.get("machine").get("id"), machineId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (priority != null) predicates.add(cb.equal(root.get("priority"), priority));
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("reportedAt"), fromDate.atStartOfDay()));
            }
            if (toDate != null) {
                predicates.add(cb.lessThan(root.get("reportedAt"), toDate.plusDays(1).atStartOfDay()));
            }
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("requestNo")), pattern),
                        cb.like(cb.lower(root.get("title")), pattern),
                        cb.like(cb.lower(root.get("description")), pattern),
                        cb.like(cb.lower(root.get("machine").get("code")), pattern),
                        cb.like(cb.lower(root.get("machine").get("name")), pattern),
                        cb.like(cb.lower(root.get("machine").get("team").get("name")), pattern),
                        cb.like(cb.lower(root.get("reportedBy").get("fullName")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<MaintenanceSchedule> scheduleSpec(
            Long machineId, Long teamId, Boolean active, LocalDate dueBefore
    ) {
        Set<Long> teams = visibleTeams(teamId);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("machine").get("team").get("id").in(teams));
            if (machineId != null) predicates.add(cb.equal(root.get("machine").get("id"), machineId));
            if (active != null) predicates.add(cb.equal(root.get("active"), active));
            if (dueBefore != null) predicates.add(cb.lessThanOrEqualTo(root.get("nextDueDate"), dueBefore));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Specification<MaintenanceWorkOrder> workOrderSpec(
            Long machineId,
            Long teamId,
            MaintenanceWorkOrderStatus status,
            MaintenancePriority priority,
            LocalDateTime from,
            LocalDateTime to
    ) {
        Set<Long> teams = visibleTeams(teamId);
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(root.get("machine").get("team").get("id").in(teams));
            if (machineId != null) predicates.add(cb.equal(root.get("machine").get("id"), machineId));
            if (status != null) predicates.add(cb.equal(root.get("status"), status));
            if (priority != null) predicates.add(cb.equal(root.get("priority"), priority));
            if (from != null) predicates.add(cb.greaterThanOrEqualTo(root.get("plannedStart"), from));
            if (to != null) predicates.add(cb.lessThan(root.get("plannedStart"), to));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private Set<Long> visibleTeams(Long teamId) {
        Set<Long> teams = authorizationScope.accessibleTeamIds();
        if (teamId == null) return teams;
        return teams.contains(teamId) ? Set.of(teamId) : Set.of();
    }

    private Machine activeMachine(Long id) {
        return machineRepository
                .findActiveByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_ID_NOT_FOUND));
    }

    private void requireMachineAccess(Long machineId) {
        if (!authorizationScope.canAccessMachine(machineId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
    }

    private MachineDowntimeStaging sourceDowntime(Long id, Machine machine) {
        if (id == null) return null;
        MachineDowntimeStaging source = downtimeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_DOWNTIME_STAGING_NOT_FOUND));
        if (!source.getMachine().getId().equals(machine.getId())) {
            throw new AppException(ErrorCode.MAINTENANCE_DOWNTIME_MACHINE_MISMATCH);
        }
        return source;
    }

    private MaintenanceRequest linkedRequest(Long id, Machine machine) {
        if (id == null) return null;
        MaintenanceRequest value = requestRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MAINTENANCE_REQUEST_NOT_FOUND));
        if (!value.getMachine().getId().equals(machine.getId()) || terminal(value.getStatus())) {
            throw new AppException(ErrorCode.INVALID_MAINTENANCE_WORK_ORDER_SOURCE);
        }
        return value;
    }

    private MaintenanceSchedule linkedSchedule(Long id, Machine machine) {
        if (id == null) return null;
        MaintenanceSchedule value = scheduleRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MAINTENANCE_SCHEDULE_NOT_FOUND));
        if (!value.getMachine().getId().equals(machine.getId()) || !Boolean.TRUE.equals(value.getActive())) {
            throw new AppException(ErrorCode.INVALID_MAINTENANCE_WORK_ORDER_SOURCE);
        }
        return value;
    }

    private Employee assignedEmployee(Long id) {
        if (id == null) return null;
        Employee employee = employeeRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ID_NOT_FOUND));
        if (!authorizationScope.canAccessEmployee(id)) throw new AppException(ErrorCode.ACCESS_DENIED);
        return employee;
    }

    private void validateScheduleDates(MaintenanceScheduleRequest input) {
        if (input.getLastCompletedDate() != null
                && input.getNextDueDate().isBefore(input.getLastCompletedDate())) {
            throw new AppException(ErrorCode.INVALID_MAINTENANCE_SCHEDULE_DATE);
        }
    }

    private void validateWorkOrderDates(LocalDateTime start, LocalDateTime end) {
        if (!end.isAfter(start)) throw new AppException(ErrorCode.INVALID_MAINTENANCE_DATE_RANGE);
    }

    private boolean validRequestTransition(MaintenanceRequestStatus from, MaintenanceRequestStatus to) {
        if (from == to) return true;
        return switch (from) {
            case OPEN -> to == MaintenanceRequestStatus.ACKNOWLEDGED || to == MaintenanceRequestStatus.CANCELLED;
            case ACKNOWLEDGED -> to == MaintenanceRequestStatus.IN_PROGRESS || to == MaintenanceRequestStatus.CANCELLED;
            case IN_PROGRESS -> to == MaintenanceRequestStatus.RESOLVED || to == MaintenanceRequestStatus.CANCELLED;
            case RESOLVED, CANCELLED -> false;
        };
    }

    private boolean validWorkOrderTransition(MaintenanceWorkOrderStatus from, MaintenanceWorkOrderStatus to) {
        if (from == to) return true;
        return switch (from) {
            case PLANNED -> to == MaintenanceWorkOrderStatus.ASSIGNED
                    || to == MaintenanceWorkOrderStatus.IN_PROGRESS
                    || to == MaintenanceWorkOrderStatus.CANCELLED;
            case ASSIGNED -> to == MaintenanceWorkOrderStatus.IN_PROGRESS
                    || to == MaintenanceWorkOrderStatus.CANCELLED;
            case IN_PROGRESS -> to == MaintenanceWorkOrderStatus.COMPLETED
                    || to == MaintenanceWorkOrderStatus.CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }

    private boolean terminal(MaintenanceRequestStatus status) {
        return status == MaintenanceRequestStatus.RESOLVED || status == MaintenanceRequestStatus.CANCELLED;
    }

    private void completeLinkedRecords(MaintenanceWorkOrder value, LocalDateTime completedAt) {
        if (value.getMaintenanceRequest() != null && !terminal(value.getMaintenanceRequest().getStatus())) {
            value.getMaintenanceRequest().setStatus(MaintenanceRequestStatus.RESOLVED);
            value.getMaintenanceRequest().setResolvedAt(completedAt);
            value.getMaintenanceRequest().setResolutionNote(value.getCompletionNote());
        }
        if (value.getMaintenanceSchedule() != null) {
            MaintenanceSchedule schedule = value.getMaintenanceSchedule();
            LocalDate completedDate = completedAt.toLocalDate();
            schedule.setLastCompletedDate(completedDate);
            schedule.setNextDueDate(completedDate.plusDays(schedule.getIntervalDays()));
        }
    }

    private void changeMachineStatus(
            Machine machine,
            MachineOperationalStatus next,
            String sourceType,
            Long sourceId,
            String note
    ) {
        MachineOperationalStatus previous = machine.getOperationalStatus();
        if (previous == next) return;
        machine.setOperationalStatus(next);
        statusHistoryRepository.save(MachineStatusHistory.builder()
                .machine(machine)
                .previousStatus(previous)
                .newStatus(next)
                .sourceType(sourceType)
                .sourceId(sourceId)
                .note(note)
                .changedBy(currentUserService.user().getUsername())
                .build());
    }

    private void requireMutableWorkOrder(MaintenanceWorkOrder value) {
        if (value.getStatus() == MaintenanceWorkOrderStatus.COMPLETED
                || value.getStatus() == MaintenanceWorkOrderStatus.CANCELLED) {
            throw new AppException(ErrorCode.MAINTENANCE_WORK_ORDER_LOCKED);
        }
    }

    private MaintenanceResponse.RequestItem requestItem(MaintenanceRequest value) {
        return MaintenanceResponse.RequestItem.builder()
                .id(value.getId())
                .requestNo(value.getRequestNo())
                .machineId(value.getMachine().getId())
                .machineCode(value.getMachine().getCode())
                .machineName(value.getMachine().getName())
                .teamId(value.getMachine().getTeam().getId())
                .teamName(value.getMachine().getTeam().getName())
                .reportedById(value.getReportedBy().getId())
                .reportedByName(value.getReportedBy().getFullName())
                .sourceDowntimeStagingId(value.getSourceDowntimeStaging() == null
                        ? null : value.getSourceDowntimeStaging().getId())
                .priority(value.getPriority())
                .status(value.getStatus())
                .title(value.getTitle())
                .description(value.getDescription())
                .impactDescription(value.getImpactDescription())
                .reportedAt(value.getReportedAt())
                .resolvedAt(value.getResolvedAt())
                .resolutionNote(value.getResolutionNote())
                .version(value.getVersion())
                .build();
    }

    private MaintenanceResponse.ScheduleItem scheduleItem(MaintenanceSchedule value) {
        return MaintenanceResponse.ScheduleItem.builder()
                .id(value.getId())
                .machineId(value.getMachine().getId())
                .machineCode(value.getMachine().getCode())
                .machineName(value.getMachine().getName())
                .teamId(value.getMachine().getTeam().getId())
                .teamName(value.getMachine().getTeam().getName())
                .maintenanceType(value.getMaintenanceType())
                .name(value.getName())
                .intervalDays(value.getIntervalDays())
                .lastCompletedDate(value.getLastCompletedDate())
                .nextDueDate(value.getNextDueDate())
                .description(value.getDescription())
                .active(value.getActive())
                .version(value.getVersion())
                .build();
    }

    private MaintenanceResponse.WorkOrderItem workOrderItem(MaintenanceWorkOrder value) {
        List<MaintenanceResponse.PartUsageItem> parts = partUsageRepository
                .findAllByWorkOrder_IdAndActiveTrue(value.getId()).stream().map(this::partItem).toList();
        BigDecimal partCost = parts.stream().map(MaintenanceResponse.PartUsageItem::getTotalCost)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return MaintenanceResponse.WorkOrderItem.builder()
                .id(value.getId())
                .workOrderNo(value.getWorkOrderNo())
                .maintenanceRequestId(value.getMaintenanceRequest() == null ? null : value.getMaintenanceRequest().getId())
                .maintenanceScheduleId(value.getMaintenanceSchedule() == null ? null : value.getMaintenanceSchedule().getId())
                .machineId(value.getMachine().getId())
                .machineCode(value.getMachine().getCode())
                .machineName(value.getMachine().getName())
                .teamId(value.getMachine().getTeam().getId())
                .teamName(value.getMachine().getTeam().getName())
                .maintenanceType(value.getMaintenanceType())
                .priority(value.getPriority())
                .status(value.getStatus())
                .assignedEmployeeId(value.getAssignedEmployee() == null ? null : value.getAssignedEmployee().getId())
                .assignedEmployeeName(value.getAssignedEmployee() == null ? null : value.getAssignedEmployee().getFullName())
                .title(value.getTitle())
                .description(value.getDescription())
                .plannedStart(value.getPlannedStart())
                .plannedEnd(value.getPlannedEnd())
                .actualStart(value.getActualStart())
                .actualEnd(value.getActualEnd())
                .laborCost(value.getLaborCost())
                .partCost(partCost)
                .externalCost(value.getExternalCost())
                .totalCost(zero(value.getLaborCost()).add(partCost).add(zero(value.getExternalCost())))
                .completionNote(value.getCompletionNote())
                .parts(parts)
                .version(value.getVersion())
                .build();
    }

    private MaintenanceResponse.PartUsageItem partItem(MaintenancePartUsage value) {
        return MaintenanceResponse.PartUsageItem.builder()
                .id(value.getId())
                .materialId(value.getMaterial().getId())
                .materialCode(value.getMaterial().getCode())
                .materialName(value.getMaterial().getName())
                .quantity(value.getQuantity())
                .unit(value.getUnit())
                .unitCost(value.getUnitCost())
                .totalCost(value.getQuantity().multiply(value.getUnitCost()))
                .build();
    }

    private MaintenanceResponse.StatusHistoryItem historyItem(MachineStatusHistory value) {
        return MaintenanceResponse.StatusHistoryItem.builder()
                .id(value.getId())
                .machineId(value.getMachine().getId())
                .previousStatus(value.getPreviousStatus())
                .newStatus(value.getNewStatus())
                .sourceType(value.getSourceType())
                .sourceId(value.getSourceId())
                .note(value.getNote())
                .changedBy(value.getChangedBy())
                .changedAt(value.getChangedAt())
                .build();
    }

    private BigDecimal totalCost(MaintenanceWorkOrder value) {
        BigDecimal partCost = partUsageRepository.findAllByWorkOrder_IdAndActiveTrue(value.getId()).stream()
                .map(part -> part.getQuantity().multiply(part.getUnitCost()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return zero(value.getLaborCost()).add(partCost).add(zero(value.getExternalCost()));
    }

    private PageRequest page(int page, int size, String sort) {
        return PageRequest.of(Math.max(0, page), Math.min(Math.max(1, size), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, sort));
    }

    private String uniqueNumber(String prefix) {
        return prefix + LocalDate.now().format(NUMBER_DATE) + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) return null;
        return value.trim();
    }

    private BigDecimal zero(BigDecimal value) {
        return value == null ? BigDecimal.ZERO : value;
    }
}
