package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.AttendanceUpsertRequest;
import com.factory.management.dto.request.EmployeeAssignmentRequest;
import com.factory.management.dto.request.EmployeeKpiRequest;
import com.factory.management.dto.request.LeaveDecisionRequest;
import com.factory.management.dto.request.NotificationCreateRequest;
import com.factory.management.dto.request.OvertimeCreateRequest;
import com.factory.management.dto.request.OvertimeDecisionRequest;
import com.factory.management.dto.request.WorkScheduleRequest;
import com.factory.management.dto.response.HrManagementResponse;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.entity.AssignmentType;
import com.factory.management.entity.AttendanceRecord;
import com.factory.management.entity.AttendanceSource;
import com.factory.management.entity.AttendanceStatus;
import com.factory.management.entity.Employee;
import com.factory.management.entity.EmployeeAssignment;
import com.factory.management.entity.EmployeeKpi;
import com.factory.management.entity.LeaveRequest;
import com.factory.management.entity.LeaveStatus;
import com.factory.management.entity.Notification;
import com.factory.management.entity.NotificationSeverity;
import com.factory.management.entity.OvertimeRequest;
import com.factory.management.entity.OvertimeStatus;
import com.factory.management.entity.Role;
import com.factory.management.entity.Shift;
import com.factory.management.entity.Team;
import com.factory.management.entity.WorkSchedule;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.repository.AttendanceRecordRepository;
import com.factory.management.repository.EmployeeAssignmentRepository;
import com.factory.management.repository.EmployeeKpiRepository;
import com.factory.management.repository.EmployeeRepository;
import com.factory.management.repository.LeaveRequestRepository;
import com.factory.management.repository.NotificationRepository;
import com.factory.management.repository.OvertimeRequestRepository;
import com.factory.management.repository.ShiftRepository;
import com.factory.management.repository.TeamRepository;
import com.factory.management.repository.WorkScheduleRepository;
import com.factory.management.security.AuthorizationScope;
import com.factory.management.service.Service.CurrentUserService;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HrManagementService {

    private static final int MAX_PAGE_SIZE = 200;
    private static final long MAX_ATTENDANCE_MINUTES = 24L * 60L;

    private final WorkScheduleRepository schedules;
    private final AttendanceRecordRepository attendance;
    private final LeaveRequestRepository leaves;
    private final EmployeeKpiRepository kpis;
    private final NotificationRepository notifications;
    private final OvertimeRequestRepository overtimeRequests;
    private final EmployeeAssignmentRepository assignments;
    private final EmployeeRepository employees;
    private final ShiftRepository shifts;
    private final TeamRepository teams;
    private final AuthorizationScope authorizationScope;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<HrManagementResponse.ScheduleItem> schedules(
            LocalDate from, LocalDate to, Long employeeId, Long teamId, int page, int size
    ) {
        DateRange range = dateRange(from, to, 31);
        if (employeeId != null) requireEmployeeAccess(employeeId);
        if (teamId != null) requireTeamAccess(teamId);
        Set<Long> visibleTeams = visibleTeams();
        boolean global = globalAccess();
        Specification<WorkSchedule> specification = (root, query, cb) -> {
            List<Predicate> values = new ArrayList<>();
            values.add(cb.isTrue(root.get("active")));
            values.add(cb.between(root.get("workDate"), range.from(), range.to()));
            if (employeeId != null) values.add(cb.equal(root.get("employee").get("id"), employeeId));
            if (teamId != null) values.add(cb.equal(root.get("employee").get("team").get("id"), teamId));
            if (!global) values.add(root.get("employee").get("team").get("id").in(visibleTeams));
            return cb.and(values.toArray(Predicate[]::new));
        };
        return PageResponse.from(schedules.findAll(specification, page(page, size, "workDate")), this::scheduleResponse);
    }

    @Transactional
    public HrManagementResponse.ScheduleItem createSchedule(WorkScheduleRequest request) {
        Employee employee = employee(request.getEmployeeId());
        requireEmployeeAccess(employee.getId());
        Shift shift = shift(request.getShiftId());
        if (schedules.existsByEmployee_IdAndWorkDateAndIdNot(employee.getId(), request.getWorkDate(), 0L)) {
            throw new AppException(ErrorCode.WORK_SCHEDULE_EXISTS);
        }
        WorkSchedule value = WorkSchedule.builder()
                .employee(employee)
                .shift(shift)
                .workDate(request.getWorkDate())
                .note(trimToNull(request.getNote()))
                .active(request.getActive() == null || request.getActive())
                .createdBy(username())
                .build();
        value = schedules.save(value);
        auditService.record("HR_SCHEDULE_CREATED", "WorkSchedule", value.getId(), employeeDetails(employee));
        return scheduleResponse(value);
    }

    @Transactional
    public HrManagementResponse.ScheduleItem updateSchedule(Long id, WorkScheduleRequest request) {
        WorkSchedule value = schedule(id);
        requireEmployeeAccess(value.getEmployee().getId());
        Employee employee = employee(request.getEmployeeId());
        requireEmployeeAccess(employee.getId());
        if (schedules.existsByEmployee_IdAndWorkDateAndIdNot(employee.getId(), request.getWorkDate(), id)) {
            throw new AppException(ErrorCode.WORK_SCHEDULE_EXISTS);
        }
        value.setEmployee(employee);
        value.setShift(shift(request.getShiftId()));
        value.setWorkDate(request.getWorkDate());
        value.setNote(trimToNull(request.getNote()));
        if (request.getActive() != null) value.setActive(request.getActive());
        auditService.record("HR_SCHEDULE_UPDATED", "WorkSchedule", value.getId(), employeeDetails(employee));
        return scheduleResponse(value);
    }

    @Transactional
    public void deleteSchedule(Long id) {
        WorkSchedule value = schedule(id);
        requireEmployeeAccess(value.getEmployee().getId());
        value.setActive(false);
        auditService.record("HR_SCHEDULE_DISABLED", "WorkSchedule", id, employeeDetails(value.getEmployee()));
    }

    @Transactional(readOnly = true)
    public PageResponse<HrManagementResponse.AttendanceItem> attendance(
            LocalDate from, LocalDate to, Long employeeId, Long teamId,
            AttendanceStatus status, int page, int size
    ) {
        DateRange range = dateRange(from, to, 31);
        if (employeeId != null) requireEmployeeAccess(employeeId);
        if (teamId != null) requireTeamAccess(teamId);
        Set<Long> visibleTeams = visibleTeams();
        boolean global = globalAccess();
        Specification<AttendanceRecord> specification = (root, query, cb) -> {
            List<Predicate> values = new ArrayList<>();
            values.add(cb.between(root.get("workDate"), range.from(), range.to()));
            if (employeeId != null) values.add(cb.equal(root.get("employee").get("id"), employeeId));
            if (teamId != null) values.add(cb.equal(root.get("employee").get("team").get("id"), teamId));
            if (status != null) values.add(cb.equal(root.get("attendanceStatus"), status));
            if (!global) values.add(root.get("employee").get("team").get("id").in(visibleTeams));
            return cb.and(values.toArray(Predicate[]::new));
        };
        return PageResponse.from(attendance.findAll(specification, page(page, size, "workDate")), this::attendanceResponse);
    }

    @Transactional
    public HrManagementResponse.AttendanceItem upsertAttendance(AttendanceUpsertRequest request) {
        Employee employee = employee(request.getEmployeeId());
        requireEmployeeAccess(employee.getId());
        AttendanceRecord value = attendance.findByEmployee_IdAndWorkDate(employee.getId(), request.getWorkDate())
                .orElseGet(() -> AttendanceRecord.builder()
                        .employee(employee)
                        .workDate(request.getWorkDate())
                        .build());
        applyAttendance(value, request);
        value.setConfirmedBy(currentUserService.employee());
        value = attendance.save(value);
        auditService.record("HR_ATTENDANCE_UPSERTED", "AttendanceRecord", value.getId(), employeeDetails(employee));
        return attendanceResponse(value);
    }

    @Transactional(readOnly = true)
    public PageResponse<HrManagementResponse.LeaveItem> leaveRequests(
            LocalDate from, LocalDate to, Long employeeId, Long teamId,
            LeaveStatus status, int page, int size
    ) {
        DateRange range = dateRange(from, to, 92);
        if (employeeId != null) requireEmployeeAccess(employeeId);
        if (teamId != null) requireTeamAccess(teamId);
        Set<Long> visibleTeams = visibleTeams();
        boolean global = globalAccess();
        Specification<LeaveRequest> specification = (root, query, cb) -> {
            List<Predicate> values = new ArrayList<>();
            values.add(cb.isTrue(root.get("active")));
            values.add(cb.lessThanOrEqualTo(root.get("fromDate"), range.to()));
            values.add(cb.greaterThanOrEqualTo(root.get("toDate"), range.from()));
            if (employeeId != null) values.add(cb.equal(root.get("employee").get("id"), employeeId));
            if (teamId != null) values.add(cb.equal(root.get("employee").get("team").get("id"), teamId));
            if (status != null) values.add(cb.equal(root.get("status"), status));
            if (!global) values.add(root.get("employee").get("team").get("id").in(visibleTeams));
            return cb.and(values.toArray(Predicate[]::new));
        };
        return PageResponse.from(leaves.findAll(specification, page(page, size, "createdAt")), this::leaveResponse);
    }

    @Transactional
    public HrManagementResponse.LeaveItem decideLeave(Long id, LeaveDecisionRequest request) {
        if (request.getDecision() != LeaveStatus.APPROVED && request.getDecision() != LeaveStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_LEAVE_DECISION);
        }
        LeaveRequest value = leaves.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.LEAVE_REQUEST_NOT_FOUND));
        requireEmployeeAccess(value.getEmployee().getId());
        if (value.getStatus() != LeaveStatus.PENDING) {
            throw new AppException(ErrorCode.LEAVE_REQUEST_NOT_PENDING);
        }
        value.setStatus(request.getDecision());
        value.setApprovedBy(currentUserService.employee());
        value.setReviewComment(trimToNull(request.getComment()));
        value.setReviewedAt(LocalDateTime.now());
        notifyDecision(value.getEmployee(), "Đơn nghỉ phép " + statusText(value.getStatus()),
                "Đơn nghỉ từ " + value.getFromDate() + " đến " + value.getToDate()
                        + " đã được " + statusText(value.getStatus()).toLowerCase() + ".",
                value.getStatus() == LeaveStatus.APPROVED ? NotificationSeverity.SUCCESS : NotificationSeverity.WARNING,
                "/workspace/employee/leave");
        auditService.record("HR_LEAVE_" + value.getStatus(), "LeaveRequest", id,
                employeeDetails(value.getEmployee()));
        return leaveResponse(value);
    }

    @Transactional(readOnly = true)
    public PageResponse<HrManagementResponse.KpiItem> kpis(
            LocalDate from, LocalDate to, Long employeeId, Long teamId, int page, int size
    ) {
        DateRange range = dateRange(from, to, 365);
        if (employeeId != null) requireEmployeeAccess(employeeId);
        if (teamId != null) requireTeamAccess(teamId);
        Set<Long> visibleTeams = visibleTeams();
        boolean global = globalAccess();
        Specification<EmployeeKpi> specification = (root, query, cb) -> {
            List<Predicate> values = new ArrayList<>();
            values.add(cb.lessThanOrEqualTo(root.get("periodStart"), range.to()));
            values.add(cb.greaterThanOrEqualTo(root.get("periodEnd"), range.from()));
            if (employeeId != null) values.add(cb.equal(root.get("employee").get("id"), employeeId));
            if (teamId != null) values.add(cb.equal(root.get("employee").get("team").get("id"), teamId));
            if (!global) values.add(root.get("employee").get("team").get("id").in(visibleTeams));
            return cb.and(values.toArray(Predicate[]::new));
        };
        return PageResponse.from(kpis.findAll(specification, page(page, size, "periodEnd")), this::kpiResponse);
    }

    @Transactional
    public HrManagementResponse.KpiItem saveKpi(Long id, EmployeeKpiRequest request) {
        Employee employee = employee(request.getEmployeeId());
        requireEmployeeAccess(employee.getId());
        validateKpi(request);
        long excludedId = id == null ? 0L : id;
        if (kpis.existsByEmployee_IdAndPeriodStartAndPeriodEndAndIdNot(
                employee.getId(), request.getPeriodStart(), request.getPeriodEnd(), excludedId)) {
            throw new AppException(ErrorCode.EMPLOYEE_KPI_EXISTS);
        }
        EmployeeKpi value = id == null
                ? EmployeeKpi.builder().createdBy(username()).build()
                : kpis.findById(id).orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_KPI_NOT_FOUND));
        if (id != null) requireEmployeeAccess(value.getEmployee().getId());
        value.setEmployee(employee);
        value.setPeriodStart(request.getPeriodStart());
        value.setPeriodEnd(request.getPeriodEnd());
        value.setProductivityScore(request.getProductivityScore());
        value.setQualityScore(request.getQualityScore());
        value.setAttendanceScore(request.getAttendanceScore());
        value.setScore(request.getScore() == null ? calculatedScore(request) : request.getScore());
        value.setNote(trimToNull(request.getNote()));
        value = kpis.save(value);
        auditService.record(id == null ? "HR_KPI_CREATED" : "HR_KPI_UPDATED", "EmployeeKpi", value.getId(),
                employeeDetails(employee));
        return kpiResponse(value);
    }

    @Transactional
    public HrManagementResponse.NotificationItem createNotification(NotificationCreateRequest request) {
        Employee employee = employee(request.getEmployeeId());
        requireEmployeeAccess(employee.getId());
        Notification value = notifications.save(Notification.builder()
                .recipient(employee)
                .title(request.getTitle().trim())
                .message(request.getMessage().trim())
                .severity(request.getSeverity() == null ? NotificationSeverity.INFO : request.getSeverity())
                .actionUrl(trimToNull(request.getActionUrl()))
                .build());
        auditService.record("HR_NOTIFICATION_CREATED", "Notification", value.getId(), employeeDetails(employee));
        return notificationResponse(value);
    }

    @Transactional
    public HrManagementResponse.OvertimeItem createOwnOvertime(OvertimeCreateRequest request) {
        request.setEmployeeId(currentUserService.employee().getId());
        return createOvertime(request);
    }

    @Transactional
    public HrManagementResponse.OvertimeItem createOvertime(OvertimeCreateRequest request) {
        Employee employee = employee(request.getEmployeeId());
        if (!employee.getId().equals(currentUserService.employee().getId())) requireEmployeeAccess(employee.getId());
        if (overtimeRequests.existsByEmployee_IdAndWorkDateAndIdNot(employee.getId(), request.getWorkDate(), 0L)) {
            throw new AppException(ErrorCode.OVERTIME_REQUEST_EXISTS);
        }
        OvertimeRequest value = overtimeRequests.save(OvertimeRequest.builder()
                .employee(employee)
                .workDate(request.getWorkDate())
                .requestedMinutes(request.getRequestedMinutes())
                .reason(request.getReason().trim())
                .build());
        auditService.record("HR_OVERTIME_CREATED", "OvertimeRequest", value.getId(), employeeDetails(employee));
        return overtimeResponse(value);
    }

    @Transactional(readOnly = true)
    public List<HrManagementResponse.OvertimeItem> ownOvertime() {
        return overtimeRequests.findAllByEmployee_IdOrderByCreatedAtDesc(currentUserService.employee().getId())
                .stream().map(this::overtimeResponse).toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<HrManagementResponse.OvertimeItem> overtime(
            LocalDate from, LocalDate to, Long employeeId, Long teamId,
            OvertimeStatus status, int page, int size
    ) {
        DateRange range = dateRange(from, to, 92);
        if (employeeId != null) requireEmployeeAccess(employeeId);
        if (teamId != null) requireTeamAccess(teamId);
        Set<Long> visibleTeams = visibleTeams();
        boolean global = globalAccess();
        Specification<OvertimeRequest> specification = (root, query, cb) -> {
            List<Predicate> values = new ArrayList<>();
            values.add(cb.between(root.get("workDate"), range.from(), range.to()));
            if (employeeId != null) values.add(cb.equal(root.get("employee").get("id"), employeeId));
            if (teamId != null) values.add(cb.equal(root.get("employee").get("team").get("id"), teamId));
            if (status != null) values.add(cb.equal(root.get("status"), status));
            if (!global) values.add(root.get("employee").get("team").get("id").in(visibleTeams));
            return cb.and(values.toArray(Predicate[]::new));
        };
        return PageResponse.from(overtimeRequests.findAll(specification, page(page, size, "createdAt")),
                this::overtimeResponse);
    }

    @Transactional
    public HrManagementResponse.OvertimeItem decideOvertime(Long id, OvertimeDecisionRequest request) {
        if (request.getDecision() != OvertimeStatus.APPROVED && request.getDecision() != OvertimeStatus.REJECTED) {
            throw new AppException(ErrorCode.INVALID_OVERTIME_DECISION);
        }
        OvertimeRequest value = overtimeRequests.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.OVERTIME_REQUEST_NOT_FOUND));
        requireEmployeeAccess(value.getEmployee().getId());
        if (value.getStatus() != OvertimeStatus.PENDING) {
            throw new AppException(ErrorCode.OVERTIME_REQUEST_NOT_PENDING);
        }
        value.setStatus(request.getDecision());
        value.setApprovedBy(currentUserService.employee());
        value.setReviewComment(trimToNull(request.getComment()));
        value.setReviewedAt(LocalDateTime.now());
        notifyDecision(value.getEmployee(), "Đăng ký tăng ca " + overtimeText(value.getStatus()),
                "Đăng ký tăng ca ngày " + value.getWorkDate() + " đã được "
                        + overtimeText(value.getStatus()).toLowerCase() + ".",
                value.getStatus() == OvertimeStatus.APPROVED ? NotificationSeverity.SUCCESS : NotificationSeverity.WARNING,
                "/workspace/employee/attendance");
        auditService.record("HR_OVERTIME_" + value.getStatus(), "OvertimeRequest", id,
                employeeDetails(value.getEmployee()));
        return overtimeResponse(value);
    }

    @Transactional(readOnly = true)
    public PageResponse<HrManagementResponse.AssignmentItem> assignments(
            LocalDate from, LocalDate to, Long employeeId, Long teamId, int page, int size
    ) {
        DateRange range = dateRange(from, to, 92);
        if (employeeId != null) requireEmployeeAccess(employeeId);
        if (teamId != null) requireTeamAccess(teamId);
        Set<Long> visibleTeams = visibleTeams();
        boolean global = globalAccess();
        Specification<EmployeeAssignment> specification = (root, query, cb) -> {
            List<Predicate> values = new ArrayList<>();
            values.add(cb.lessThanOrEqualTo(root.get("effectiveFrom"), range.to()));
            values.add(cb.or(cb.isNull(root.get("effectiveTo")),
                    cb.greaterThanOrEqualTo(root.get("effectiveTo"), range.from())));
            if (employeeId != null) values.add(cb.equal(root.get("employee").get("id"), employeeId));
            if (teamId != null) values.add(cb.equal(root.get("targetTeam").get("id"), teamId));
            if (!global) values.add(root.get("targetTeam").get("id").in(visibleTeams));
            return cb.and(values.toArray(Predicate[]::new));
        };
        return PageResponse.from(assignments.findAll(specification, page(page, size, "effectiveFrom")),
                this::assignmentResponse);
    }

    @Transactional
    public HrManagementResponse.AssignmentItem createAssignment(EmployeeAssignmentRequest request) {
        if (request.getAssignmentType() != AssignmentType.TRANSFERRED
                && request.getAssignmentType() != AssignmentType.SUPPORT) {
            throw new AppException(ErrorCode.INVALID_ASSIGNMENT_TYPE);
        }
        if (request.getEffectiveTo() != null && request.getEffectiveTo().isBefore(request.getEffectiveFrom())) {
            throw new AppException(ErrorCode.INVALID_ASSIGNMENT_DATE_RANGE);
        }
        Employee employee = employee(request.getEmployeeId());
        requireEmployeeAccess(employee.getId());
        Team target = teams
                .findActiveByIdInActiveHierarchy(request.getTargetTeamId())
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_ID_NOT_FOUND));
        requireTeamAccess(target.getId());
        LocalDate end = request.getEffectiveTo() == null ? LocalDate.of(9999, 12, 31) : request.getEffectiveTo();
        boolean overlap = assignments.findAll((root, query, cb) -> cb.and(
                cb.equal(root.get("employee").get("id"), employee.getId()),
                cb.isTrue(root.get("active")),
                cb.lessThanOrEqualTo(root.get("effectiveFrom"), end),
                cb.or(cb.isNull(root.get("effectiveTo")),
                        cb.greaterThanOrEqualTo(root.get("effectiveTo"), request.getEffectiveFrom()))
        )).stream().findAny().isPresent();
        if (overlap) throw new AppException(ErrorCode.EMPLOYEE_ASSIGNMENT_OVERLAP);

        EmployeeAssignment value = assignments.save(EmployeeAssignment.builder()
                .employee(employee)
                .sourceTeam(employee.getTeam())
                .targetTeam(target)
                .assignmentType(request.getAssignmentType())
                .effectiveFrom(request.getEffectiveFrom())
                .effectiveTo(request.getEffectiveTo())
                .reason(request.getReason().trim())
                .approvedBy(currentUserService.employee())
                .build());
        auditService.record("HR_ASSIGNMENT_CREATED", "EmployeeAssignment", value.getId(),
                "{\"employeeId\":" + employee.getId() + ",\"targetTeamId\":" + target.getId() + "}");
        return assignmentResponse(value);
    }

    @Transactional
    public void endAssignment(Long id, LocalDate endDate) {
        EmployeeAssignment value = assignments.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ASSIGNMENT_NOT_FOUND));
        requireEmployeeAccess(value.getEmployee().getId());
        requireTeamAccess(value.getTargetTeam().getId());
        if (endDate.isBefore(value.getEffectiveFrom())) {
            throw new AppException(ErrorCode.INVALID_ASSIGNMENT_DATE_RANGE);
        }
        value.setEffectiveTo(endDate);
        value.setActive(false);
        auditService.record("HR_ASSIGNMENT_ENDED", "EmployeeAssignment", id, employeeDetails(value.getEmployee()));
    }

    private void applyAttendance(AttendanceRecord value, AttendanceUpsertRequest request) {
        AttendanceStatus status = request.getAttendanceStatus();
        value.setAttendanceStatus(status);
        value.setSource(request.getSource() == null ? AttendanceSource.MANUAL : request.getSource());
        value.setNote(trimToNull(request.getNote()));
        if (status == AttendanceStatus.ABSENT || status == AttendanceStatus.ON_LEAVE) {
            value.setCheckIn(null);
            value.setCheckOut(null);
            value.setWorkingMinutes(0);
            value.setOvertimeMinutes(0);
            return;
        }
        if (request.getCheckIn() == null || request.getCheckOut() == null) {
            throw new AppException(ErrorCode.ATTENDANCE_TIME_REQUIRED);
        }
        if (!request.getCheckIn().toLocalDate().equals(request.getWorkDate())
                || !request.getCheckOut().isAfter(request.getCheckIn())) {
            throw new AppException(ErrorCode.ATTENDANCE_INVALID_TIME_RANGE);
        }
        long minutes = Duration.between(request.getCheckIn(), request.getCheckOut()).toMinutes();
        if (minutes <= 0 || minutes > MAX_ATTENDANCE_MINUTES) {
            throw new AppException(ErrorCode.ATTENDANCE_INVALID_TIME_RANGE);
        }
        int scheduledMinutes = schedules.findByEmployee_IdAndWorkDateAndActiveTrue(
                        value.getEmployee().getId(), request.getWorkDate())
                .map(schedule -> shiftMinutes(schedule.getShift()))
                .orElse((int) minutes);
        value.setCheckIn(request.getCheckIn());
        value.setCheckOut(request.getCheckOut());
        value.setWorkingMinutes((int) minutes);
        value.setOvertimeMinutes(Math.max(0, (int) minutes - scheduledMinutes));
    }

    private void validateKpi(EmployeeKpiRequest request) {
        if (request.getPeriodEnd().isBefore(request.getPeriodStart())) {
            throw new AppException(ErrorCode.INVALID_KPI_PERIOD);
        }
        if (request.getScore() == null && request.getProductivityScore() == null
                && request.getQualityScore() == null && request.getAttendanceScore() == null) {
            throw new AppException(ErrorCode.INVALID_KPI_SCORE);
        }
    }

    private BigDecimal calculatedScore(EmployeeKpiRequest request) {
        List<BigDecimal> values = java.util.stream.Stream.of(
                        request.getProductivityScore(), request.getQualityScore(), request.getAttendanceScore())
                .filter(java.util.Objects::nonNull).toList();
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private DateRange dateRange(LocalDate from, LocalDate to, int defaultDays) {
        LocalDate resolvedFrom = from == null ? LocalDate.now().minusDays(defaultDays) : from;
        LocalDate resolvedTo = to == null ? LocalDate.now().plusDays(defaultDays) : to;
        if (resolvedFrom.isAfter(resolvedTo) || Duration.between(
                resolvedFrom.atStartOfDay(), resolvedTo.plusDays(1).atStartOfDay()).toDays() > 732) {
            throw new AppException(ErrorCode.INVALID_PRODUCTION_REPORT_DATE_RANGE);
        }
        return new DateRange(resolvedFrom, resolvedTo);
    }

    private PageRequest page(int page, int size, String sort) {
        return PageRequest.of(Math.max(page, 0), Math.min(Math.max(size, 1), MAX_PAGE_SIZE),
                Sort.by(Sort.Direction.DESC, sort));
    }

    private void requireEmployeeAccess(Long employeeId) {
        if (!authorizationScope.canAccessEmployee(employeeId)) throw new AppException(ErrorCode.ACCESS_DENIED);
    }

    private void requireTeamAccess(Long teamId) {
        if (!authorizationScope.canAccessTeam(teamId)) throw new AppException(ErrorCode.ACCESS_DENIED);
    }

    private Set<Long> visibleTeams() {
        return authorizationScope.accessibleTeamIds();
    }

    private boolean globalAccess() {
        Set<Role> roles = authorizationScope.currentRoles();
        return roles.contains(Role.ADMIN) || roles.contains(Role.DIRECTOR);
    }

    private Employee employee(Long id) {
        return employees.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ID_NOT_FOUND));
    }

    private Shift shift(Long id) {
        return shifts.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_ID_NOT_FOUND));
    }

    private WorkSchedule schedule(Long id) {
        return schedules.findById(id).orElseThrow(() -> new AppException(ErrorCode.WORK_SCHEDULE_NOT_FOUND));
    }

    private int shiftMinutes(Shift shift) {
        long minutes = Duration.between(shift.getStartTime(), shift.getEndTime()).toMinutes();
        if (minutes <= 0) minutes += 24L * 60L;
        return (int) minutes;
    }

    private void notifyDecision(
            Employee recipient, String title, String message, NotificationSeverity severity, String actionUrl
    ) {
        notifications.save(Notification.builder()
                .recipient(recipient)
                .title(title)
                .message(message)
                .severity(severity)
                .actionUrl(actionUrl)
                .build());
    }

    private HrManagementResponse.ScheduleItem scheduleResponse(WorkSchedule value) {
        Employee employee = value.getEmployee();
        return HrManagementResponse.ScheduleItem.builder()
                .id(value.getId()).employeeId(employee.getId()).employeeCode(employee.getCode())
                .employeeName(employee.getFullName()).teamId(teamId(employee)).teamName(teamName(employee))
                .shiftId(value.getShift().getId()).shiftCode(value.getShift().getCode())
                .shiftName(value.getShift().getName()).workDate(value.getWorkDate()).note(value.getNote())
                .active(value.getActive()).version(value.getVersion()).build();
    }

    private HrManagementResponse.AttendanceItem attendanceResponse(AttendanceRecord value) {
        Employee employee = value.getEmployee();
        return HrManagementResponse.AttendanceItem.builder()
                .id(value.getId()).employeeId(employee.getId()).employeeCode(employee.getCode())
                .employeeName(employee.getFullName()).teamId(teamId(employee)).teamName(teamName(employee))
                .workDate(value.getWorkDate()).checkIn(value.getCheckIn()).checkOut(value.getCheckOut())
                .workingMinutes(value.getWorkingMinutes()).overtimeMinutes(value.getOvertimeMinutes())
                .attendanceStatus(value.getAttendanceStatus()).source(value.getSource()).note(value.getNote())
                .confirmedByName(value.getConfirmedBy() == null ? null : value.getConfirmedBy().getFullName())
                .version(value.getVersion()).build();
    }

    private HrManagementResponse.LeaveItem leaveResponse(LeaveRequest value) {
        Employee employee = value.getEmployee();
        return HrManagementResponse.LeaveItem.builder()
                .id(value.getId()).employeeId(employee.getId()).employeeCode(employee.getCode())
                .employeeName(employee.getFullName()).teamId(teamId(employee)).teamName(teamName(employee))
                .fromDate(value.getFromDate()).toDate(value.getToDate()).leaveType(value.getLeaveType())
                .reason(value.getReason()).status(value.getStatus())
                .approvedByName(value.getApprovedBy() == null ? null : value.getApprovedBy().getFullName())
                .reviewComment(value.getReviewComment()).reviewedAt(value.getReviewedAt())
                .createdAt(value.getCreatedAt()).version(value.getVersion()).build();
    }

    private HrManagementResponse.KpiItem kpiResponse(EmployeeKpi value) {
        Employee employee = value.getEmployee();
        return HrManagementResponse.KpiItem.builder()
                .id(value.getId()).employeeId(employee.getId()).employeeCode(employee.getCode())
                .employeeName(employee.getFullName()).teamId(teamId(employee)).teamName(teamName(employee))
                .periodStart(value.getPeriodStart()).periodEnd(value.getPeriodEnd()).score(value.getScore())
                .productivityScore(value.getProductivityScore()).qualityScore(value.getQualityScore())
                .attendanceScore(value.getAttendanceScore()).note(value.getNote()).version(value.getVersion()).build();
    }

    private HrManagementResponse.OvertimeItem overtimeResponse(OvertimeRequest value) {
        Employee employee = value.getEmployee();
        return HrManagementResponse.OvertimeItem.builder()
                .id(value.getId()).employeeId(employee.getId()).employeeCode(employee.getCode())
                .employeeName(employee.getFullName()).teamId(teamId(employee)).teamName(teamName(employee))
                .workDate(value.getWorkDate()).requestedMinutes(value.getRequestedMinutes()).reason(value.getReason())
                .status(value.getStatus())
                .approvedByName(value.getApprovedBy() == null ? null : value.getApprovedBy().getFullName())
                .reviewComment(value.getReviewComment()).reviewedAt(value.getReviewedAt())
                .createdAt(value.getCreatedAt()).version(value.getVersion()).build();
    }

    private HrManagementResponse.AssignmentItem assignmentResponse(EmployeeAssignment value) {
        Employee employee = value.getEmployee();
        return HrManagementResponse.AssignmentItem.builder()
                .id(value.getId()).employeeId(employee.getId()).employeeCode(employee.getCode())
                .employeeName(employee.getFullName())
                .sourceTeamId(value.getSourceTeam() == null ? null : value.getSourceTeam().getId())
                .sourceTeamName(value.getSourceTeam() == null ? null : value.getSourceTeam().getName())
                .targetTeamId(value.getTargetTeam().getId()).targetTeamName(value.getTargetTeam().getName())
                .assignmentType(value.getAssignmentType()).effectiveFrom(value.getEffectiveFrom())
                .effectiveTo(value.getEffectiveTo()).reason(value.getReason())
                .approvedByName(value.getApprovedBy().getFullName()).active(value.getActive())
                .version(value.getVersion()).build();
    }

    private HrManagementResponse.NotificationItem notificationResponse(Notification value) {
        return HrManagementResponse.NotificationItem.builder()
                .id(value.getId()).recipientEmployeeId(value.getRecipient().getId())
                .recipientName(value.getRecipient().getFullName()).title(value.getTitle()).message(value.getMessage())
                .severity(value.getSeverity()).actionUrl(value.getActionUrl()).read(value.getRead())
                .readAt(value.getReadAt()).createdAt(value.getCreatedAt()).build();
    }

    private Long teamId(Employee employee) {
        return employee.getTeam() == null ? null : employee.getTeam().getId();
    }

    private String teamName(Employee employee) {
        return employee.getTeam() == null ? null : employee.getTeam().getName();
    }

    private String username() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private String employeeDetails(Employee employee) {
        return "{\"employeeId\":" + employee.getId() + "}";
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String statusText(LeaveStatus status) {
        return status == LeaveStatus.APPROVED ? "Đã duyệt" : "Bị từ chối";
    }

    private String overtimeText(OvertimeStatus status) {
        return status == OvertimeStatus.APPROVED ? "Đã duyệt" : "Bị từ chối";
    }

    private record DateRange(LocalDate from, LocalDate to) {
    }
}
