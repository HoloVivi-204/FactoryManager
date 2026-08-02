package com.factory.management.ai.tool;

import com.factory.management.ai.AiDashboard;
import com.factory.management.ai.AiDataTool;
import com.factory.management.ai.AiToolArguments;
import com.factory.management.ai.AiToolContext;
import com.factory.management.ai.AiToolResult;
import com.factory.management.entity.AttendanceRecord;
import com.factory.management.entity.AttendanceStatus;
import com.factory.management.entity.EmployeeActual;
import com.factory.management.entity.EmployeeActualStaging;
import com.factory.management.entity.LeaveRequest;
import com.factory.management.entity.LeaveStatus;
import com.factory.management.entity.MachineDowntime;
import com.factory.management.entity.MachineDowntimeStaging;
import com.factory.management.entity.MaintenanceRequest;
import com.factory.management.entity.MaintenanceRequestStatus;
import com.factory.management.entity.MaterialIssue;
import com.factory.management.entity.MaterialIssueStaging;
import com.factory.management.entity.ProductionReport;
import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.ProductionReportStatus;
import com.factory.management.entity.QualityReport;
import com.factory.management.entity.QualityReportStaging;
import com.factory.management.entity.Role;
import com.factory.management.entity.WorkSchedule;
import com.factory.management.repository.AttendanceRecordRepository;
import com.factory.management.repository.EmployeeActualRepository;
import com.factory.management.repository.EmployeeActualStagingRepository;
import com.factory.management.repository.LeaveRequestRepository;
import com.factory.management.repository.MachineDowntimeRepository;
import com.factory.management.repository.MachineDowntimeStagingRepository;
import com.factory.management.repository.MaintenanceRequestRepository;
import com.factory.management.repository.MaterialIssueRepository;
import com.factory.management.repository.MaterialIssueStagingRepository;
import com.factory.management.repository.ProductionReportRepository;
import com.factory.management.repository.ProductionReportStagingRepository;
import com.factory.management.repository.QualityReportRepository;
import com.factory.management.repository.QualityReportStagingRepository;
import com.factory.management.repository.WorkScheduleRepository;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class OperationalDetailsTool implements AiDataTool {
    private static final Set<Role> ALLOWED = Set.of(
            Role.ADMIN, Role.DIRECTOR, Role.FACTORY_MANAGER, Role.DEPARTMENT_MANAGER,
            Role.PRODUCTION_MANAGER, Role.TEAM_LEADER
    );

    private final ScopedProductionQuery productionQuery;
    private final ProductionReportRepository productionReportRepository;
    private final ProductionReportStagingRepository stagingReportRepository;
    private final MachineDowntimeRepository downtimeRepository;
    private final MachineDowntimeStagingRepository downtimeStagingRepository;
    private final EmployeeActualRepository employeeActualRepository;
    private final EmployeeActualStagingRepository employeeActualStagingRepository;
    private final QualityReportRepository qualityRepository;
    private final QualityReportStagingRepository qualityStagingRepository;
    private final MaterialIssueRepository materialRepository;
    private final MaterialIssueStagingRepository materialStagingRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final LeaveRequestRepository leaveRequestRepository;
    private final MaintenanceRequestRepository maintenanceRequestRepository;
    private final WorkScheduleRepository workScheduleRepository;

    private enum DetailType {
        MACHINE_DOWNTIME,
        ATTENDANCE_EXCEPTIONS,
        QUALITY_ERRORS,
        MATERIAL_ISSUES
    }

    @Override
    public String name() {
        return "get_operational_details";
    }

    @Override
    public String description() {
        return "Tra cứu danh sách chi tiết theo ngày trong phạm vi được cấp: máy dừng/hỏng và nguyên nhân, "
                + "nhân viên vắng/nghỉ/đi muộn/về sớm, lỗi chất lượng hoặc sự cố vật tư. "
                + "Dùng Tool này khi người dùng hỏi 'máy nào', 'nhân viên nào', 'lỗi gì', 'vật tư gì'. "
                + "Với nhân sự, tổng người của ca phải lấy từ work_schedule; không lấy tổng dòng ngoại lệ làm mẫu số. "
                + "Nếu hỏi tình hình ca hôm nay, đặt includeTemporary=true để kiểm tra riêng dữ liệu chưa duyệt; "
                + "đặt unplannedOnly=true khi câu hỏi nói máy lỗi, hỏng hoặc sự cố ngoài kế hoạch.";
    }

    @Override
    public Map<String, Object> parametersSchema() {
        Map<String, Object> properties = new LinkedHashMap<>();
        properties.put("detailType", AiToolArguments.requiredEnum(
                "Loại chi tiết cần lấy. Bắt buộc MACHINE_DOWNTIME khi câu hỏi nói máy lỗi/hỏng/dừng/sự cố; "
                        + "chỉ chọn QUALITY_ERRORS khi nói rõ lỗi chất lượng hoặc lỗi sản phẩm.", List.of(
                        "MACHINE_DOWNTIME", "ATTENDANCE_EXCEPTIONS", "QUALITY_ERRORS", "MATERIAL_ISSUES")));
        properties.put("fromDate", AiToolArguments.nullableString("Ngày bắt đầu ISO yyyy-MM-dd; null nghĩa là hôm nay."));
        properties.put("toDate", AiToolArguments.nullableString("Ngày kết thúc ISO yyyy-MM-dd; null nghĩa là fromDate."));
        properties.put("factory", AiToolArguments.nullableString("Mã hoặc tên nhà máy; null nếu không lọc."));
        properties.put("department", AiToolArguments.nullableString("Mã hoặc tên phòng ban; null nếu không lọc."));
        properties.put("productionLine", AiToolArguments.nullableString("Mã hoặc tên dây chuyền; null nếu không lọc."));
        properties.put("team", AiToolArguments.nullableString("Mã hoặc tên tổ; null nếu không lọc."));
        properties.put("shift", AiToolArguments.nullableString("Mã hoặc tên ca làm việc; null nếu không lọc."));
        properties.put("machine", AiToolArguments.nullableString("Mã hoặc tên máy; null nếu không lọc."));
        properties.put("keyword", AiToolArguments.nullableString(
                "Tên/mã cụ thể của máy, nhân viên, nguyên nhân, loại lỗi hoặc vật tư cần tìm; "
                        + "không đưa các từ chung như 'máy lỗi', 'máy hỏng', 'máy nào' vào keyword."));
        properties.put("attendanceStatus", AiToolArguments.nullableString(
                "PRESENT, ABSENT, LATE, LEAVE_EARLY hoặc ON_LEAVE; null lấy mọi trạng thái bất thường."));
        properties.put("unplannedOnly", AiToolArguments.nullableBoolean(
                "true chỉ lấy dừng máy ngoài kế hoạch; null/false lấy mọi lần dừng."));
        properties.put("includeTemporary", AiToolArguments.nullableBoolean(
                "true khi cần xem thêm báo cáo tạm/chưa duyệt, đặc biệt cho ca hôm nay."));
        properties.put("limit", AiToolArguments.nullableInteger("Số dòng tối đa từ 1 đến 100; null là 50.", 1, 100));
        return AiToolArguments.objectSchema(properties);
    }

    @Override
    public Set<Role> allowedRoles() {
        return ALLOWED;
    }

    @Override
    @Transactional(readOnly = true)
    public AiToolResult execute(Map<String, Object> arguments, AiToolContext context) {
        DetailType detailType = detailType(arguments);
        LocalDate from = AiToolArguments.date(arguments, "fromDate", context.today());
        LocalDate to = AiToolArguments.date(arguments, "toDate", from);
        AiToolArguments.validatePeriod(from, to, 366);
        int limit = AiToolArguments.integer(arguments, "limit", 50, 1, 100);
        String keyword = AiToolArguments.text(arguments, "keyword");
        AttendanceStatus attendanceStatus = detailType == DetailType.ATTENDANCE_EXCEPTIONS
                ? attendanceStatus(arguments) : null;
        boolean unplannedOnly = detailType == DetailType.MACHINE_DOWNTIME
                && AiToolArguments.bool(arguments, "unplannedOnly", false);
        boolean temporaryRequested = AiToolArguments.bool(arguments, "includeTemporary", false);
        boolean periodContainsToday = !context.today().isBefore(from) && !context.today().isAfter(to);
        boolean includeTemporary = temporaryRequested || periodContainsToday;

        String factory = AiToolArguments.text(arguments, "factory");
        String department = AiToolArguments.text(arguments, "department");
        String productionLine = AiToolArguments.text(arguments, "productionLine");
        String team = AiToolArguments.text(arguments, "team");
        String shift = AiToolArguments.text(arguments, "shift");
        String machine = AiToolArguments.text(arguments, "machine");

        List<ProductionReport> officialReports = productionQuery.find(
                context, from, to,
                factory, department, productionLine, team, machine).stream()
                .filter(report -> matches(report.getShift().getCode(), report.getShift().getName(), shift))
                .toList();
        Set<Long> officialReportIds = officialReports.stream()
                .map(ProductionReport::getId).collect(Collectors.toSet());
        String reportKeyword = detailType == DetailType.MACHINE_DOWNTIME
                || detailType == DetailType.ATTENDANCE_EXCEPTIONS ? null : keyword;
        List<Map<String, Object>> officialRows = new ArrayList<>(officialRows(
                detailType, officialReportIds, attendanceStatus, unplannedOnly, reportKeyword));

        List<Map<String, Object>> temporaryRows = new ArrayList<>();
        if (includeTemporary) {
            List<ProductionReportStaging> temporaryReports = temporaryReports(context, from, to, arguments);
            Set<Long> temporaryReportIds = temporaryReports.stream()
                    .map(ProductionReportStaging::getId).collect(Collectors.toSet());
            temporaryRows.addAll(temporaryRows(
                    detailType, temporaryReportIds, attendanceStatus, unplannedOnly, reportKeyword));
        }

        Map<String, Integer> sourceCounts = new LinkedHashMap<>();
        countSources(sourceCounts, officialRows);
        countSources(sourceCounts, temporaryRows);
        int conflictCount = 0;
        List<Map<String, Object>> dashboardPopulation = null;
        Map<String, Object> attendanceOverview = null;

        if (detailType == DetailType.MACHINE_DOWNTIME) {
            List<Map<String, Object>> maintenanceRows = maintenanceRequestRows(
                    context, from, to, factory, department, productionLine, team, machine);
            countSources(sourceCounts, maintenanceRows);
            officialRows = mergeMaintenanceRows(officialRows, maintenanceRows);
            officialRows = filterAndSort(officialRows, keyword);
            temporaryRows = filterAndSort(temporaryRows, keyword);
        } else if (detailType == DetailType.ATTENDANCE_EXCEPTIONS) {
            List<Map<String, Object>> scheduleRows = workScheduleRows(
                    context, from, to, factory, department, productionLine, team, shift);
            Map<String, Map<String, Object>> scheduleLookup = scheduleLookup(scheduleRows);
            List<Map<String, Object>> attendanceRows = attendanceRecordRows(
                    context, from, to, factory, department, productionLine, team);
            List<Map<String, Object>> leaveRows = approvedLeaveRows(
                    context, from, to, factory, department, productionLine, team);
            attendanceRows = enrichWithSchedule(attendanceRows, scheduleLookup, shift);
            leaveRows = enrichWithSchedule(leaveRows, scheduleLookup, shift);
            officialRows = enrichWithSchedule(officialRows, scheduleLookup, shift);
            temporaryRows = enrichWithSchedule(temporaryRows, scheduleLookup, shift);
            countSources(sourceCounts, attendanceRows);
            countSources(sourceCounts, leaveRows);
            countSources(sourceCounts, scheduleRows);
            AttendanceMerge merged = mergeAttendanceRows(
                    attendanceRows, leaveRows, officialRows, temporaryRows, scheduleRows,
                    attendanceStatus, keyword);
            officialRows = merged.officialRows();
            temporaryRows = merged.temporaryRows();
            conflictCount = merged.conflictCount();
            dashboardPopulation = merged.populationRows().stream()
                    .filter(row -> Boolean.TRUE.equals(row.get("scheduled")))
                    .toList();
            attendanceOverview = attendanceOverview(
                    merged.populationRows(), dashboardPopulation, from, to);
        }

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("period", Map.of("fromDate", from, "toDate", to));
        data.put("detailType", detailType.name());
        data.put("sourceRecordCounts", sourceCounts);
        data.put("official", section(detailType, officialRows, limit));
        if (includeTemporary) {
            data.put("temporaryUnconfirmed", section(detailType, temporaryRows, limit));
        }
        if (attendanceOverview != null) {
            data.put("attendanceOverview", attendanceOverview);
        }

        String dataStatus = status(officialRows, temporaryRows);
        List<String> warnings = new ArrayList<>();
        if (context.accessibleTeamIds().isEmpty()) {
            warnings.add("Tài khoản chưa được cấp phạm vi tổ/dây chuyền để tra cứu dữ liệu vận hành.");
        }
        if (periodContainsToday && !temporaryRequested) {
            warnings.add("Khoảng ngày có hôm nay nên hệ thống đã tự đối chiếu thêm dữ liệu staging chưa chốt.");
        }
        if (includeTemporary && !temporaryRows.isEmpty()) {
            warnings.add("Dữ liệu tạm/chưa xác nhận được hiển thị riêng, không được xem là số liệu chính thức.");
        }
        if (detailType == DetailType.MACHINE_DOWNTIME) {
            warnings.add("Máy dừng/hỏng được đối chiếu từ machine_downtime và maintenance_request; "
                    + "yêu cầu bảo trì không có thời lượng dừng máy sẽ không được cộng vào tổng số phút dừng.");
        }
        if (detailType == DetailType.ATTENDANCE_EXCEPTIONS) {
            warnings.add("Nhân sự được đối chiếu theo thứ tự ưu tiên: work_schedule làm mẫu số; "
                    + "attendance_record, leave_request APPROVED, employee_actual chính thức "
                    + "rồi employee_actual_staging làm trạng thái thực tế.");
            if (dashboardPopulation == null || dashboardPopulation.isEmpty()) {
                warnings.add("Chưa có lịch làm phù hợp ngày/ca/phạm vi nên hệ thống không tạo tỷ lệ "
                        + "nhân sự trên tổng; các bản ghi thực tế chỉ dùng để tra cứu chi tiết.");
            } else {
                long unconfirmed = dashboardPopulation.stream()
                        .filter(row -> "NO_ATTENDANCE_RECORD".equals(row.get("attendanceStatus")))
                        .count();
                if (unconfirmed > 0) {
                    warnings.add("Có " + unconfirmed
                            + " lượt người-ca đã được xếp lịch nhưng chưa có dữ liệu chấm công; "
                            + "hệ thống không tự coi họ là vắng mặt.");
                }
            }
            if (conflictCount > 0) {
                warnings.add("Có " + conflictCount
                        + " bản ghi nhân sự khác nhau cho cùng nhân viên/ngày; "
                        + "hệ thống đã giữ nguồn có độ ưu tiên cao hơn.");
            }
        }
        if (officialRows.isEmpty() && temporaryRows.isEmpty()) {
            warnings.add("Không có bản ghi phù hợp ngày, loại dữ liệu và phạm vi được hỏi.");
        }

        List<AiToolResult.Source> sources = sources(sourceCounts);

        List<Map<String, Object>> dashboardRows = Stream.concat(
                        officialRows.stream(), temporaryRows.stream())
                .limit(limit)
                .toList();
        AiDashboard dashboard = dashboard(
                detailType, from, to, officialRows, temporaryRows, dashboardRows,
                dashboardPopulation, attendanceOverview);
        return new AiToolResult(name(), dataStatus, data, sources, warnings, dashboard);
    }

    private DetailType detailType(Map<String, Object> arguments) {
        try {
            return DetailType.valueOf(String.valueOf(arguments.get("detailType")));
        } catch (RuntimeException exception) {
            throw new com.factory.management.exception.AppException(
                    com.factory.management.exception.ErrorCode.AI_TOOL_ARGUMENT_INVALID);
        }
    }

    private AttendanceStatus attendanceStatus(Map<String, Object> arguments) {
        String value = AiToolArguments.text(arguments, "attendanceStatus");
        if (value == null) return null;
        try {
            return AttendanceStatus.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new com.factory.management.exception.AppException(
                    com.factory.management.exception.ErrorCode.AI_TOOL_ARGUMENT_INVALID);
        }
    }

    private List<ProductionReportStaging> temporaryReports(
            AiToolContext context,
            LocalDate from,
            LocalDate to,
            Map<String, Object> arguments
    ) {
        if (context.accessibleTeamIds().isEmpty()) return List.of();
        String factory = AiToolArguments.text(arguments, "factory");
        String department = AiToolArguments.text(arguments, "department");
        String line = AiToolArguments.text(arguments, "productionLine");
        String team = AiToolArguments.text(arguments, "team");
        String shift = AiToolArguments.text(arguments, "shift");
        String machine = AiToolArguments.text(arguments, "machine");
        return stagingReportRepository
                .findAllByTeam_IdInAndReportDateBetweenOrderByReportDateDescIdDesc(
                        context.accessibleTeamIds(), from, to)
                .stream()
                .filter(value -> value.getStatus() != ProductionReportStatus.APPROVED
                        && value.getStatus() != ProductionReportStatus.LOCKED
                        && value.getStatus() != ProductionReportStatus.IMPORTED)
                .filter(value -> !productionReportRepository.existsBySourceStaging_Id(value.getId()))
                .filter(value -> matches(value.getFactory().getCode(), value.getFactory().getName(), factory))
                .filter(value -> matches(value.getDepartment().getCode(), value.getDepartment().getName(), department))
                .filter(value -> matches(value.getProductionLine().getCode(), value.getProductionLine().getName(), line))
                .filter(value -> matches(value.getTeam().getCode(), value.getTeam().getName(), team))
                .filter(value -> matches(value.getShift().getCode(), value.getShift().getName(), shift))
                .filter(value -> matches(value.getMachine().getCode(), value.getMachine().getName(), machine))
                .toList();
    }

    private boolean matches(String code, String name, String selector) {
        if (selector == null || selector.isBlank()) return true;
        String value = AiToolArguments.normalized(selector);
        return AiToolArguments.normalized(code).contains(value)
                || AiToolArguments.normalized(name).contains(value);
    }

    private List<Map<String, Object>> workScheduleRows(
            AiToolContext context,
            LocalDate from,
            LocalDate to,
            String factory,
            String department,
            String productionLine,
            String team,
            String shift
    ) {
        if (context.accessibleTeamIds().isEmpty()) return List.of();
        return workScheduleRepository.findAll((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.isTrue(root.get("active")));
                    predicates.add(cb.between(root.<LocalDate>get("workDate"), from, to));
                    Path<?> teamPath = root.get("employee").get("team");
                    predicates.add(teamPath.get("id").in(context.accessibleTeamIds()));
                    addOrganizationSelectors(
                            predicates, cb, teamPath, factory, department, productionLine, team);
                    addSelector(predicates, cb, root.get("shift"), shift);
                    query.orderBy(
                            cb.asc(root.get("workDate")),
                            cb.asc(root.get("shift").get("id")),
                            cb.asc(root.get("id")));
                    return cb.and(predicates.toArray(Predicate[]::new));
                }).stream()
                .map(this::workScheduleRow)
                .toList();
    }

    private Map<String, Map<String, Object>> scheduleLookup(List<Map<String, Object>> schedules) {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>();
        for (Map<String, Object> schedule : schedules) {
            result.putIfAbsent(employeeDateKey(schedule), schedule);
        }
        return result;
    }

    private List<Map<String, Object>> enrichWithSchedule(
            List<Map<String, Object>> rows,
            Map<String, Map<String, Object>> schedules,
            String shiftSelector
    ) {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> original : rows) {
            Map<String, Object> row = new LinkedHashMap<>(original);
            Map<String, Object> schedule = schedules.get(employeeDateKey(row));
            Long currentShiftId = longValue(row.get("shiftId"));
            if (schedule != null) {
                Long scheduledShiftId = longValue(schedule.get("shiftId"));
                if (currentShiftId == null || Objects.equals(currentShiftId, scheduledShiftId)) {
                    copyScheduleContext(row, schedule);
                    row.put("scheduled", true);
                } else {
                    row.put("scheduled", false);
                }
                result.add(row);
                continue;
            }
            row.put("scheduled", false);
            if (shiftSelector == null || currentShiftId != null) {
                result.add(row);
            }
        }
        return result;
    }

    private void copyScheduleContext(Map<String, Object> target, Map<String, Object> schedule) {
        target.put("scheduleId", schedule.get("scheduleId"));
        target.put("shiftId", schedule.get("shiftId"));
        target.put("shiftCode", schedule.get("shiftCode"));
        target.put("shift", schedule.get("shift"));
        target.put("teamId", schedule.get("teamId"));
        target.put("team", schedule.get("team"));
    }

    private String employeeDateKey(Map<String, Object> row) {
        return row.get("employeeId") + "|" + row.get("reportDate");
    }

    private String attendanceKey(Map<String, Object> row) {
        return employeeDateKey(row) + "|" + row.get("shiftId");
    }

    private List<Map<String, Object>> attendanceRecordRows(
            AiToolContext context,
            LocalDate from,
            LocalDate to,
            String factory,
            String department,
            String productionLine,
            String team
    ) {
        if (context.accessibleTeamIds().isEmpty()) return List.of();
        return attendanceRecordRepository.findAll((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.between(root.<LocalDate>get("workDate"), from, to));
                    Path<?> teamPath = root.get("employee").get("team");
                    predicates.add(teamPath.get("id").in(context.accessibleTeamIds()));
                    addOrganizationSelectors(
                            predicates, cb, teamPath, factory, department, productionLine, team);
                    query.orderBy(cb.asc(root.get("workDate")), cb.asc(root.get("id")));
                    return cb.and(predicates.toArray(Predicate[]::new));
                }).stream()
                .map(this::attendanceRecordRow)
                .toList();
    }

    private List<Map<String, Object>> approvedLeaveRows(
            AiToolContext context,
            LocalDate from,
            LocalDate to,
            String factory,
            String department,
            String productionLine,
            String team
    ) {
        if (context.accessibleTeamIds().isEmpty()) return List.of();
        List<LeaveRequest> requests = leaveRequestRepository.findAll((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            predicates.add(cb.isTrue(root.get("active")));
            predicates.add(cb.equal(root.get("status"), LeaveStatus.APPROVED));
            predicates.add(cb.lessThanOrEqualTo(root.<LocalDate>get("fromDate"), to));
            predicates.add(cb.greaterThanOrEqualTo(root.<LocalDate>get("toDate"), from));
            Path<?> teamPath = root.get("employee").get("team");
            predicates.add(teamPath.get("id").in(context.accessibleTeamIds()));
            addOrganizationSelectors(
                    predicates, cb, teamPath, factory, department, productionLine, team);
            query.orderBy(cb.asc(root.get("fromDate")), cb.asc(root.get("id")));
            return cb.and(predicates.toArray(Predicate[]::new));
        });

        List<Map<String, Object>> rows = new ArrayList<>();
        for (LeaveRequest request : requests) {
            LocalDate first = request.getFromDate().isAfter(from) ? request.getFromDate() : from;
            LocalDate last = request.getToDate().isBefore(to) ? request.getToDate() : to;
            for (LocalDate date = first; !date.isAfter(last); date = date.plusDays(1)) {
                rows.add(approvedLeaveRow(request, date));
            }
        }
        return rows;
    }

    private List<Map<String, Object>> maintenanceRequestRows(
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
        LocalDateTime fromTime = from.atStartOfDay();
        LocalDateTime toExclusive = to.plusDays(1).atStartOfDay();
        return maintenanceRequestRepository.findAll((root, query, cb) -> {
                    List<Predicate> predicates = new ArrayList<>();
                    predicates.add(cb.isTrue(root.get("active")));
                    predicates.add(cb.notEqual(root.get("status"), MaintenanceRequestStatus.CANCELLED));
                    predicates.add(cb.lessThan(root.<LocalDateTime>get("reportedAt"), toExclusive));
                    predicates.add(cb.or(
                            cb.isNull(root.get("resolvedAt")),
                            cb.greaterThanOrEqualTo(root.<LocalDateTime>get("resolvedAt"), fromTime)
                    ));
                    Path<?> machinePath = root.get("machine");
                    Path<?> teamPath = machinePath.get("team");
                    predicates.add(teamPath.get("id").in(context.accessibleTeamIds()));
                    addSelector(predicates, cb, machinePath, machine);
                    addOrganizationSelectors(
                            predicates, cb, teamPath, factory, department, productionLine, team);
                    query.orderBy(cb.asc(root.get("reportedAt")), cb.asc(root.get("id")));
                    return cb.and(predicates.toArray(Predicate[]::new));
                }).stream()
                .map(this::maintenanceRequestRow)
                .toList();
    }

    private void addOrganizationSelectors(
            List<Predicate> predicates,
            CriteriaBuilder cb,
            Path<?> teamPath,
            String factory,
            String department,
            String productionLine,
            String team
    ) {
        addSelector(predicates, cb, teamPath, team);
        Path<?> linePath = teamPath.get("productionLine");
        addSelector(predicates, cb, linePath, productionLine);
        Path<?> departmentPath = linePath.get("department");
        addSelector(predicates, cb, departmentPath, department);
        addSelector(predicates, cb, departmentPath.get("factory"), factory);
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
        predicates.add(cb.or(
                cb.equal(code, value),
                cb.equal(name, value),
                cb.like(code, "%" + value + "%"),
                cb.like(name, "%" + value + "%")
        ));
    }

    private List<Map<String, Object>> mergeMaintenanceRows(
            List<Map<String, Object>> reportRows,
            List<Map<String, Object>> maintenanceRows
    ) {
        List<Map<String, Object>> result = new ArrayList<>(reportRows);
        Map<Long, Map<String, Object>> officialBySourceStaging = new HashMap<>();
        for (Map<String, Object> row : reportRows) {
            Long sourceId = longValue(row.get("sourceStagingId"));
            if (sourceId != null) officialBySourceStaging.putIfAbsent(sourceId, row);
        }
        for (Map<String, Object> maintenance : maintenanceRows) {
            Long sourceId = longValue(maintenance.get("sourceDowntimeStagingId"));
            Map<String, Object> linked = sourceId == null ? null : officialBySourceStaging.get(sourceId);
            if (linked == null) {
                result.add(maintenance);
                continue;
            }
            linked.put("recordSource", "MACHINE_DOWNTIME+MAINTENANCE_REQUEST");
            linked.put("maintenanceRequestId", maintenance.get("maintenanceRequestId"));
            linked.put("maintenanceRequestNo", maintenance.get("maintenanceRequestNo"));
            linked.put("maintenanceStatus", maintenance.get("maintenanceStatus"));
            linked.put("maintenancePriority", maintenance.get("maintenancePriority"));
            linked.put("maintenanceTitle", maintenance.get("maintenanceTitle"));
            linked.put("impactDescription", maintenance.get("impactDescription"));
            linked.put("resolvedAt", maintenance.get("resolvedAt"));
            linked.put("resolutionNote", maintenance.get("resolutionNote"));
        }
        return result;
    }

    private AttendanceMerge mergeAttendanceRows(
            List<Map<String, Object>> attendanceRows,
            List<Map<String, Object>> leaveRows,
            List<Map<String, Object>> officialReportRows,
            List<Map<String, Object>> temporaryReportRows,
            List<Map<String, Object>> scheduleRows,
            AttendanceStatus requestedStatus,
            String keyword
    ) {
        Map<String, Map<String, Object>> selected = new LinkedHashMap<>();
        int conflicts = 0;
        for (List<Map<String, Object>> source : List.of(
                attendanceRows, leaveRows, officialReportRows, temporaryReportRows)) {
            for (Map<String, Object> candidate : source) {
                String key = attendanceKey(candidate);
                Map<String, Object> existing = selected.putIfAbsent(key, candidate);
                if (existing != null && attendanceConflict(existing, candidate)) conflicts++;
            }
        }

        for (Map<String, Object> schedule : scheduleRows) {
            selected.putIfAbsent(attendanceKey(schedule), schedule);
        }

        List<Map<String, Object>> population = filterAndSort(
                new ArrayList<>(selected.values()), null);
        List<Map<String, Object>> matched = population.stream()
                .filter(row -> isAttendanceStatus(row.get("attendanceStatus")))
                .filter(row -> attendanceMatches(
                        AttendanceStatus.valueOf(String.valueOf(row.get("attendanceStatus"))), requestedStatus))
                .toList();
        List<Map<String, Object>> official = filterAndSort(matched.stream()
                .filter(row -> !"TEMPORARY_UNCONFIRMED".equals(row.get("dataStatus")))
                .toList(), keyword);
        List<Map<String, Object>> temporary = filterAndSort(matched.stream()
                .filter(row -> "TEMPORARY_UNCONFIRMED".equals(row.get("dataStatus")))
                .toList(), keyword);
        return new AttendanceMerge(official, temporary, population, conflicts);
    }

    private boolean isAttendanceStatus(Object value) {
        if (value == null) return false;
        try {
            AttendanceStatus.valueOf(String.valueOf(value));
            return true;
        } catch (IllegalArgumentException exception) {
            return false;
        }
    }

    private boolean attendanceConflict(Map<String, Object> first, Map<String, Object> second) {
        return !Objects.equals(first.get("attendanceStatus"), second.get("attendanceStatus"))
                || !Objects.equals(first.get("workingMinutes"), second.get("workingMinutes"))
                || !Objects.equals(first.get("overtimeMinutes"), second.get("overtimeMinutes"));
    }

    private Long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private record AttendanceMerge(
            List<Map<String, Object>> officialRows,
            List<Map<String, Object>> temporaryRows,
            List<Map<String, Object>> populationRows,
            int conflictCount
    ) {
    }

    private Map<String, Object> attendanceOverview(
            List<Map<String, Object>> population,
            List<Map<String, Object>> scheduledRows,
            LocalDate from,
            LocalDate to
    ) {
        Map<String, AttendanceAggregate> groups = new LinkedHashMap<>();
        for (Map<String, Object> row : scheduledRows) {
            groups.computeIfAbsent(attendanceGroupKey(row), ignored -> new AttendanceAggregate(row))
                    .addScheduled(row);
        }
        for (Map<String, Object> row : population) {
            if (Boolean.TRUE.equals(row.get("scheduled")) || !isWorkingAttendance(row)) continue;
            AttendanceAggregate group = groups.get(attendanceGroupKey(row));
            if (group != null) group.addSupport(row);
        }

        int planned = groups.values().stream().mapToInt(group -> group.planned).sum();
        int scheduledWorking = groups.values().stream().mapToInt(group -> group.scheduledWorking).sum();
        int support = groups.values().stream().mapToInt(group -> group.support).sum();
        int absent = groups.values().stream().mapToInt(group -> group.absent).sum();
        int onLeave = groups.values().stream().mapToInt(group -> group.onLeave).sum();
        int late = groups.values().stream().mapToInt(group -> group.late).sum();
        int leaveEarly = groups.values().stream().mapToInt(group -> group.leaveEarly).sum();
        int unconfirmed = groups.values().stream().mapToInt(group -> group.unconfirmed).sum();
        int overtime = groups.values().stream().mapToInt(group -> group.overtime).sum();
        int actualWithoutSchedule = (int) population.stream()
                .filter(row -> !Boolean.TRUE.equals(row.get("scheduled")))
                .filter(this::isWorkingAttendance)
                .count();

        String unit = groups.size() == 1 && from.equals(to) ? "người" : "lượt người-ca";
        String context = groups.size() == 1
                ? groups.values().iterator().next().context()
                : groups.size() + " nhóm ngày-ca-tổ từ " + from + " đến " + to;
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("denominatorAvailable", planned > 0);
        result.put("denominatorSource", "WORK_SCHEDULE");
        result.put("unit", unit);
        result.put("context", context);
        result.put("plannedHeadcount", planned);
        result.put("scheduledWorkingHeadcount", scheduledWorking);
        result.put("actualWorkingHeadcount", scheduledWorking + support);
        result.put("absent", absent);
        result.put("onLeave", onLeave);
        result.put("late", late);
        result.put("leaveEarly", leaveEarly);
        result.put("unconfirmed", unconfirmed);
        result.put("overtime", overtime);
        result.put("support", support);
        result.put("actualWithoutSchedule", actualWithoutSchedule);
        result.put("groups", groups.values().stream().map(AttendanceAggregate::toMap).toList());
        return result;
    }

    private String attendanceGroupKey(Map<String, Object> row) {
        return row.get("reportDate") + "|" + row.get("shiftId") + "|" + row.get("teamId");
    }

    private boolean isWorkingAttendance(Map<String, Object> row) {
        String status = String.valueOf(row.get("attendanceStatus"));
        return "PRESENT".equals(status)
                || "LATE".equals(status)
                || "LEAVE_EARLY".equals(status)
                || intValue(row.get("workingMinutes")) > 0;
    }

    private boolean hasOvertime(Map<String, Object> row) {
        return intValue(row.get("overtimeMinutes")) > 0
                || "OVERTIME".equals(row.get("assignmentType"));
    }

    private int intValue(Object value) {
        return value instanceof Number number ? number.intValue() : 0;
    }

    private final class AttendanceAggregate {
        private final Object date;
        private final Object shiftId;
        private final Object shift;
        private final Object teamId;
        private final Object team;
        private int planned;
        private int scheduledWorking;
        private int present;
        private int absent;
        private int onLeave;
        private int late;
        private int leaveEarly;
        private int unconfirmed;
        private int overtime;
        private int support;

        private AttendanceAggregate(Map<String, Object> row) {
            date = row.get("reportDate");
            shiftId = row.get("shiftId");
            shift = row.get("shift");
            teamId = row.get("teamId");
            team = row.get("team");
        }

        private void addScheduled(Map<String, Object> row) {
            planned++;
            String status = String.valueOf(row.get("attendanceStatus"));
            if (isWorkingAttendance(row)) scheduledWorking++;
            switch (status) {
                case "PRESENT" -> present++;
                case "ABSENT" -> absent++;
                case "ON_LEAVE" -> onLeave++;
                case "LATE" -> late++;
                case "LEAVE_EARLY" -> leaveEarly++;
                case "NO_ATTENDANCE_RECORD" -> unconfirmed++;
                default -> {
                }
            }
            if (hasOvertime(row)) overtime++;
        }

        private void addSupport(Map<String, Object> row) {
            support++;
            if (hasOvertime(row)) overtime++;
        }

        private String context() {
            return date + " · " + shift + " · " + team;
        }

        private Map<String, Object> toMap() {
            Map<String, Object> value = new LinkedHashMap<>();
            value.put("date", date);
            value.put("shiftId", shiftId);
            value.put("shift", shift);
            value.put("teamId", teamId);
            value.put("team", team);
            value.put("plannedHeadcount", planned);
            value.put("scheduledWorkingHeadcount", scheduledWorking);
            value.put("actualWorkingHeadcount", scheduledWorking + support);
            value.put("present", present);
            value.put("absent", absent);
            value.put("onLeave", onLeave);
            value.put("late", late);
            value.put("leaveEarly", leaveEarly);
            value.put("unconfirmed", unconfirmed);
            value.put("overtime", overtime);
            value.put("support", support);
            return value;
        }
    }

    private List<Map<String, Object>> officialRows(
            DetailType type,
            Set<Long> reportIds,
            AttendanceStatus attendanceStatus,
            boolean unplannedOnly,
            String keyword
    ) {
        if (reportIds.isEmpty()) return List.of();
        List<Map<String, Object>> rows = switch (type) {
            case MACHINE_DOWNTIME -> downtimeRepository.findAllByProductionReport_IdIn(reportIds).stream()
                    .filter(value -> !unplannedOnly
                            || "UNPLANNED".equals(value.getDowntimeReason().getReasonType().name()))
                    .map(this::officialDowntimeRow).toList();
            case ATTENDANCE_EXCEPTIONS -> employeeActualRepository.findAllByProductionReport_IdIn(reportIds).stream()
                    .map(this::officialAttendanceRow).toList();
            case QUALITY_ERRORS -> qualityRepository.findAllByProductionReport_IdIn(reportIds).stream()
                    .map(this::officialQualityRow).toList();
            case MATERIAL_ISSUES -> materialRepository.findAllByProductionReport_IdIn(reportIds).stream()
                    .map(this::officialMaterialRow).toList();
        };
        return filterAndSort(rows, keyword);
    }

    private List<Map<String, Object>> temporaryRows(
            DetailType type,
            Set<Long> reportIds,
            AttendanceStatus attendanceStatus,
            boolean unplannedOnly,
            String keyword
    ) {
        if (reportIds.isEmpty()) return List.of();
        List<Map<String, Object>> rows = switch (type) {
            case MACHINE_DOWNTIME -> downtimeStagingRepository
                    .findAllByProductionReportStaging_IdInAndActiveTrue(reportIds).stream()
                    .filter(value -> !unplannedOnly
                            || "UNPLANNED".equals(value.getDowntimeReason().getReasonType().name()))
                    .map(this::temporaryDowntimeRow).toList();
            case ATTENDANCE_EXCEPTIONS -> employeeActualStagingRepository
                    .findAllByProductionReportStaging_IdInAndActiveTrue(reportIds).stream()
                    .map(this::temporaryAttendanceRow).toList();
            case QUALITY_ERRORS -> qualityStagingRepository
                    .findAllByProductionReportStaging_IdInAndActiveTrue(reportIds).stream()
                    .map(this::temporaryQualityRow).toList();
            case MATERIAL_ISSUES -> materialStagingRepository
                    .findAllByProductionReportStaging_IdInAndActiveTrue(reportIds).stream()
                    .map(this::temporaryMaterialRow).toList();
        };
        return filterAndSort(rows, keyword);
    }

    private boolean attendanceMatches(AttendanceStatus current, AttendanceStatus requested) {
        if (requested != null) return current == requested;
        return current != AttendanceStatus.PRESENT;
    }

    private List<Map<String, Object>> filterAndSort(List<Map<String, Object>> rows, String keyword) {
        String term = AiToolArguments.normalized(keyword);
        return rows.stream()
                .filter(row -> term == null || row.values().stream()
                        .anyMatch(value -> AiToolArguments.normalized(String.valueOf(value)).contains(term)))
                .sorted(Comparator
                        .comparing((Map<String, Object> row) -> String.valueOf(row.get("reportDate")))
                        .thenComparing(row -> String.valueOf(row.getOrDefault("startTime", "")))
                        .thenComparing(row -> String.valueOf(row.getOrDefault("displayName", ""))))
                .toList();
    }

    private Map<String, Object> baseOfficial(ProductionReport report) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("reportId", report.getId());
        row.put("reportNo", report.getReportNo());
        row.put("reportDate", report.getReportDate());
        row.put("shiftId", report.getShift().getId());
        row.put("shiftCode", report.getShift().getCode());
        row.put("shift", report.getShift().getName());
        row.put("teamId", report.getTeam().getId());
        row.put("team", report.getTeam().getName());
        row.put("dataStatus", "OFFICIAL");
        return row;
    }

    private Map<String, Object> baseTemporary(ProductionReportStaging report) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("reportId", report.getId());
        row.put("reportNo", "STAGING-" + report.getId());
        row.put("reportDate", report.getReportDate());
        row.put("shiftId", report.getShift().getId());
        row.put("shiftCode", report.getShift().getCode());
        row.put("shift", report.getShift().getName());
        row.put("teamId", report.getTeam().getId());
        row.put("team", report.getTeam().getName());
        row.put("reportStatus", report.getStatus().name());
        row.put("dataStatus", "TEMPORARY_UNCONFIRMED");
        return row;
    }

    private Map<String, Object> workScheduleRow(WorkSchedule value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("scheduleId", value.getId());
        row.put("reportDate", value.getWorkDate());
        row.put("shiftId", value.getShift().getId());
        row.put("shiftCode", value.getShift().getCode());
        row.put("shift", value.getShift().getName());
        row.put("teamId", value.getEmployee().getTeam().getId());
        row.put("team", value.getEmployee().getTeam().getName());
        row.put("dataStatus", "OFFICIAL");
        row.put("recordSource", "WORK_SCHEDULE");
        row.put("scheduled", true);
        row.put("employeeId", value.getEmployee().getId());
        row.put("employeeCode", value.getEmployee().getCode());
        row.put("employeeName", value.getEmployee().getFullName());
        row.put("displayName", value.getEmployee().getCode() + " - " + value.getEmployee().getFullName());
        row.put("position", value.getEmployee().getPosition());
        row.put("attendanceStatus", "NO_ATTENDANCE_RECORD");
        row.put("attendanceStatusLabel", "Chưa có chấm công");
        row.put("assignmentType", null);
        row.put("workingMinutes", 0);
        row.put("overtimeMinutes", 0);
        row.put("description", value.getNote());
        return row;
    }

    private Map<String, Object> attendanceRecordRow(AttendanceRecord value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("reportDate", value.getWorkDate());
        row.put("shiftId", null);
        row.put("shiftCode", null);
        row.put("shift", null);
        row.put("teamId", value.getEmployee().getTeam().getId());
        row.put("team", value.getEmployee().getTeam().getName());
        row.put("dataStatus", "OFFICIAL");
        row.put("recordSource", "ATTENDANCE_RECORD");
        row.put("attendanceRecordId", value.getId());
        addAttendance(row, value.getEmployee().getId(), value.getEmployee().getCode(),
                value.getEmployee().getFullName(), value.getEmployee().getPosition(),
                value.getAttendanceStatus(), null, value.getWorkingMinutes(),
                value.getOvertimeMinutes(), value.getNote());
        row.put("checkIn", value.getCheckIn());
        row.put("checkOut", value.getCheckOut());
        row.put("attendanceSource", value.getSource().name());
        row.put("confirmedBy", value.getConfirmedBy() == null
                ? null : value.getConfirmedBy().getFullName());
        return row;
    }

    private Map<String, Object> approvedLeaveRow(LeaveRequest value, LocalDate date) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("reportDate", date);
        row.put("shiftId", null);
        row.put("shiftCode", null);
        row.put("shift", null);
        row.put("teamId", value.getEmployee().getTeam().getId());
        row.put("team", value.getEmployee().getTeam().getName());
        row.put("dataStatus", "OFFICIAL");
        row.put("recordSource", "LEAVE_REQUEST");
        row.put("leaveRequestId", value.getId());
        addAttendance(row, value.getEmployee().getId(), value.getEmployee().getCode(),
                value.getEmployee().getFullName(), value.getEmployee().getPosition(),
                AttendanceStatus.ON_LEAVE, null, 0, 0, value.getReason());
        row.put("leaveType", value.getLeaveType());
        row.put("leaveStatus", value.getStatus().name());
        row.put("leaveFromDate", value.getFromDate());
        row.put("leaveToDate", value.getToDate());
        return row;
    }

    private Map<String, Object> maintenanceRequestRow(MaintenanceRequest value) {
        Map<String, Object> row = new LinkedHashMap<>();
        row.put("reportDate", value.getReportedAt().toLocalDate());
        row.put("shift", null);
        row.put("team", value.getMachine().getTeam().getName());
        row.put("dataStatus", "OFFICIAL");
        row.put("recordSource", "MAINTENANCE_REQUEST");
        addDowntime(row, value.getMachine().getId(), value.getMachine().getCode(),
                value.getMachine().getName(), value.getTitle(), "MAINTENANCE_REQUEST",
                value.getReportedAt(), value.getResolvedAt(), 0, value.getDescription());
        row.put("maintenanceRequestId", value.getId());
        row.put("maintenanceRequestNo", value.getRequestNo());
        row.put("maintenanceStatus", value.getStatus().name());
        row.put("maintenancePriority", value.getPriority().name());
        row.put("maintenanceTitle", value.getTitle());
        row.put("impactDescription", value.getImpactDescription());
        row.put("reportedBy", value.getReportedBy().getFullName());
        row.put("reportedAt", value.getReportedAt());
        row.put("resolvedAt", value.getResolvedAt());
        row.put("resolutionNote", value.getResolutionNote());
        row.put("sourceDowntimeStagingId", value.getSourceDowntimeStaging() == null
                ? null : value.getSourceDowntimeStaging().getId());
        return row;
    }

    private Map<String, Object> officialDowntimeRow(MachineDowntime value) {
        Map<String, Object> row = baseOfficial(value.getProductionReport());
        row.put("recordSource", "MACHINE_DOWNTIME");
        row.put("detailId", value.getId());
        row.put("sourceStagingId", value.getSourceStaging().getId());
        addDowntime(row, value.getMachine().getId(), value.getMachine().getCode(), value.getMachine().getName(),
                value.getDowntimeReason().getName(), value.getDowntimeReason().getReasonType().name(),
                value.getStartTime(), value.getEndTime(), value.getDurationMinutes(), value.getDescription());
        return row;
    }

    private Map<String, Object> temporaryDowntimeRow(MachineDowntimeStaging value) {
        Map<String, Object> row = baseTemporary(value.getProductionReportStaging());
        row.put("recordSource", "MACHINE_DOWNTIME_STAGING");
        row.put("detailId", value.getId());
        row.put("sourceStagingId", value.getId());
        addDowntime(row, value.getMachine().getId(), value.getMachine().getCode(), value.getMachine().getName(),
                value.getDowntimeReason().getName(), value.getDowntimeReason().getReasonType().name(),
                value.getStartTime(), value.getEndTime(), value.getDurationMinutes(), value.getDescription());
        return row;
    }

    private void addDowntime(
            Map<String, Object> row, Long machineId, String code, String name,
            String reason, String reasonType, Object start, Object end, Integer minutes, String description
    ) {
        row.put("machineId", machineId);
        row.put("machineCode", code);
        row.put("machineName", name);
        row.put("displayName", code + " - " + name);
        row.put("reason", reason);
        row.put("reasonType", reasonType);
        row.put("startTime", start);
        row.put("endTime", end);
        row.put("durationMinutes", minutes);
        row.put("description", description);
    }

    private Map<String, Object> officialAttendanceRow(EmployeeActual value) {
        Map<String, Object> row = baseOfficial(value.getProductionReport());
        row.put("recordSource", "EMPLOYEE_ACTUAL");
        row.put("detailId", value.getId());
        row.put("sourceStagingId", value.getSourceStaging().getId());
        addAttendance(row, value.getEmployee().getId(), value.getEmployee().getCode(),
                value.getEmployee().getFullName(), value.getEmployee().getPosition(),
                value.getAttendanceStatus(), value.getAssignmentType().name(), value.getWorkingMinutes(),
                value.getOvertimeMinutes(), value.getDescription());
        return row;
    }

    private Map<String, Object> temporaryAttendanceRow(EmployeeActualStaging value) {
        Map<String, Object> row = baseTemporary(value.getProductionReportStaging());
        row.put("recordSource", "EMPLOYEE_ACTUAL_STAGING");
        row.put("detailId", value.getId());
        row.put("sourceStagingId", value.getId());
        addAttendance(row, value.getEmployee().getId(), value.getEmployee().getCode(),
                value.getEmployee().getFullName(), value.getEmployee().getPosition(),
                value.getAttendanceStatus(), value.getAssignmentType().name(), value.getWorkingMinutes(),
                value.getOvertimeMinutes(), value.getDescription());
        return row;
    }

    private void addAttendance(
            Map<String, Object> row, Long employeeId, String code, String name, String position,
            AttendanceStatus status, String assignment, Integer working, Integer overtime, String description
    ) {
        row.put("employeeId", employeeId);
        row.put("employeeCode", code);
        row.put("employeeName", name);
        row.put("displayName", code + " - " + name);
        row.put("position", position);
        row.put("attendanceStatus", status.name());
        row.put("attendanceStatusLabel", attendanceLabel(status));
        row.put("assignmentType", assignment);
        row.put("workingMinutes", working);
        row.put("overtimeMinutes", overtime);
        row.put("description", description);
    }

    private Map<String, Object> officialQualityRow(QualityReport value) {
        Map<String, Object> row = baseOfficial(value.getProductionReport());
        row.put("recordSource", "QUALITY_REPORT");
        row.put("detailId", value.getId());
        addQuality(row, value.getQualityErrorType().getCode(), value.getQualityErrorType().getName(),
                value.getQualityErrorType().getSeverity().name(), value.getQuantity(), value.getDescription());
        return row;
    }

    private Map<String, Object> temporaryQualityRow(QualityReportStaging value) {
        Map<String, Object> row = baseTemporary(value.getProductionReportStaging());
        row.put("recordSource", "QUALITY_REPORT_STAGING");
        row.put("detailId", value.getId());
        addQuality(row, value.getQualityErrorType().getCode(), value.getQualityErrorType().getName(),
                value.getQualityErrorType().getSeverity().name(), value.getQuantity(), value.getDescription());
        return row;
    }

    private void addQuality(
            Map<String, Object> row, String code, String name, String severity, Long quantity, String description
    ) {
        row.put("errorTypeCode", code);
        row.put("errorTypeName", name);
        row.put("displayName", code + " - " + name);
        row.put("severity", severity);
        row.put("quantity", quantity);
        row.put("description", description);
    }

    private Map<String, Object> officialMaterialRow(MaterialIssue value) {
        Map<String, Object> row = baseOfficial(value.getProductionReport());
        row.put("recordSource", "MATERIAL_ISSUE");
        row.put("detailId", value.getId());
        addMaterial(row, value.getMaterial().getId(), value.getMaterial().getCode(), value.getMaterial().getName(),
                value.getIssueType().name(), value.getQuantity(), value.getUnit(), value.getDescription());
        return row;
    }

    private Map<String, Object> temporaryMaterialRow(MaterialIssueStaging value) {
        Map<String, Object> row = baseTemporary(value.getProductionReportStaging());
        row.put("recordSource", "MATERIAL_ISSUE_STAGING");
        row.put("detailId", value.getId());
        addMaterial(row, value.getMaterial().getId(), value.getMaterial().getCode(), value.getMaterial().getName(),
                value.getIssueType().name(), value.getQuantity(), value.getUnit(), value.getDescription());
        return row;
    }

    private void addMaterial(
            Map<String, Object> row, Long id, String code, String name, String issueType,
            BigDecimal quantity, String unit, String description
    ) {
        row.put("materialId", id);
        row.put("materialCode", code);
        row.put("materialName", name);
        row.put("displayName", code + " - " + name);
        row.put("issueType", issueType);
        row.put("quantity", quantity);
        row.put("unit", unit);
        row.put("description", description);
    }

    private Map<String, Object> section(DetailType type, List<Map<String, Object>> rows, int limit) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordCount", rows.size());
        result.put("summary", summary(type, rows));
        result.put("rows", rows.stream().limit(limit).toList());
        return result;
    }

    private Map<String, Object> summary(DetailType type, List<Map<String, Object>> rows) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("recordCount", rows.size());
        switch (type) {
            case MACHINE_DOWNTIME -> {
                result.put("uniqueMachines", unique(rows, "machineId"));
                result.put("totalDurationMinutes", sumLong(rows, "durationMinutes"));
                result.put("unplannedEvents", rows.stream()
                        .filter(row -> "UNPLANNED".equals(row.get("reasonType"))).count());
            }
            case ATTENDANCE_EXCEPTIONS -> {
                result.put("uniqueEmployees", unique(rows, "employeeId"));
                for (AttendanceStatus status : AttendanceStatus.values()) {
                    result.put(status.name().toLowerCase(), rows.stream()
                            .filter(row -> status.name().equals(row.get("attendanceStatus"))).count());
                }
            }
            case QUALITY_ERRORS -> {
                result.put("errorTypeCount", unique(rows, "errorTypeCode"));
                result.put("totalDefectQuantity", sumLong(rows, "quantity"));
            }
            case MATERIAL_ISSUES -> {
                result.put("materialCount", unique(rows, "materialId"));
                result.put("issueTypeCount", unique(rows, "issueType"));
            }
        }
        return result;
    }

    private long unique(List<Map<String, Object>> rows, String key) {
        return rows.stream().map(row -> row.get(key)).filter(java.util.Objects::nonNull).distinct().count();
    }

    private long sumLong(List<Map<String, Object>> rows, String key) {
        return rows.stream().map(row -> row.get(key)).filter(java.util.Objects::nonNull)
                .mapToLong(value -> value instanceof Number number ? number.longValue() : 0L).sum();
    }

    private void countSources(Map<String, Integer> counts, List<Map<String, Object>> rows) {
        for (Map<String, Object> row : rows) {
            Object source = row.get("recordSource");
            if (source != null) counts.merge(String.valueOf(source), 1, Integer::sum);
        }
    }

    private List<AiToolResult.Source> sources(Map<String, Integer> counts) {
        LocalDateTime now = LocalDateTime.now();
        return counts.entrySet().stream()
                .map(entry -> new AiToolResult.Source(
                        entry.getKey(), sourceLabel(entry.getKey()), sourceStatus(entry.getKey()),
                        entry.getValue(), now))
                .toList();
    }

    private String sourceStatus(String source) {
        return source.endsWith("_STAGING") ? "TEMPORARY_UNCONFIRMED" : "OFFICIAL";
    }

    private String sourceLabel(String source) {
        return switch (source) {
            case "WORK_SCHEDULE" -> "Lịch làm việc dùng làm mẫu số nhân sự";
            case "ATTENDANCE_RECORD" -> "Chấm công theo ngày";
            case "LEAVE_REQUEST" -> "Đơn nghỉ phép đã duyệt";
            case "EMPLOYEE_ACTUAL" -> "Nhân sự thực tế trong báo cáo chính thức";
            case "EMPLOYEE_ACTUAL_STAGING" -> "Nhân sự thực tế trong báo cáo tạm";
            case "MACHINE_DOWNTIME" -> "Chi tiết dừng máy chính thức";
            case "MACHINE_DOWNTIME_STAGING" -> "Chi tiết dừng máy tạm";
            case "MAINTENANCE_REQUEST" -> "Yêu cầu bảo trì trực tiếp";
            case "QUALITY_REPORT" -> "Chi tiết lỗi chất lượng chính thức";
            case "QUALITY_REPORT_STAGING" -> "Chi tiết lỗi chất lượng tạm";
            case "MATERIAL_ISSUE" -> "Chi tiết sự cố vật tư chính thức";
            case "MATERIAL_ISSUE_STAGING" -> "Chi tiết sự cố vật tư tạm";
            default -> source;
        };
    }

    private String status(List<Map<String, Object>> official, List<Map<String, Object>> temporary) {
        if (official.isEmpty() && temporary.isEmpty()) return "NO_BUSINESS_DATA";
        if (official.isEmpty()) return "TEMPORARY_UNCONFIRMED";
        if (!temporary.isEmpty()) return "OFFICIAL_WITH_TEMPORARY";
        return "OFFICIAL";
    }

    private AiDashboard dashboard(
            DetailType type,
            LocalDate from,
            LocalDate to,
            List<Map<String, Object>> official,
            List<Map<String, Object>> temporary,
            List<Map<String, Object>> rows,
            List<Map<String, Object>> dashboardPopulation,
            Map<String, Object> attendanceOverview
    ) {
        List<Map<String, Object>> all = Stream.concat(official.stream(), temporary.stream()).toList();
        List<Map<String, Object>> chartRows = type == DetailType.ATTENDANCE_EXCEPTIONS
                && dashboardPopulation != null ? dashboardPopulation : all;
        Map<String, Object> metrics = summary(type, all);
        List<AiDashboard.Kpi> kpis = new ArrayList<>();
        kpis.add(new AiDashboard.Kpi("Bản ghi", all.size(), "", "blue"));
        kpis.add(switch (type) {
            case MACHINE_DOWNTIME -> new AiDashboard.Kpi(
                    "Tổng dừng", metrics.get("totalDurationMinutes"), "phút", "orange");
            case ATTENDANCE_EXCEPTIONS -> new AiDashboard.Kpi(
                    "Nhân viên ảnh hưởng", metrics.get("uniqueEmployees"), "người", "orange");
            case QUALITY_ERRORS -> new AiDashboard.Kpi(
                    "Sản phẩm lỗi", metrics.get("totalDefectQuantity"), "sản phẩm", "red");
            case MATERIAL_ISSUES -> new AiDashboard.Kpi(
                    "Vật tư ảnh hưởng", metrics.get("materialCount"), "loại", "purple");
        });
        kpis.add(new AiDashboard.Kpi("Chính thức", official.size(), "bản ghi", "green"));
        kpis.add(new AiDashboard.Kpi("Chưa xác nhận", temporary.size(), "bản ghi", "purple"));
        return new AiDashboard(
                dashboardTitle(type),
                "Từ " + from + " đến " + to + " · dữ liệu theo phạm vi tài khoản",
                kpis,
                bars(type, chartRows),
                columns(type),
                rows,
                ratios(type, attendanceOverview)
        );
    }

    private List<AiDashboard.Ratio> ratios(
            DetailType type,
            Map<String, Object> attendanceOverview
    ) {
        if (type != DetailType.ATTENDANCE_EXCEPTIONS || attendanceOverview == null
                || !Boolean.TRUE.equals(attendanceOverview.get("denominatorAvailable"))) {
            return List.of();
        }
        int total = intValue(attendanceOverview.get("plannedHeadcount"));
        String unit = String.valueOf(attendanceOverview.get("unit"));
        String context = String.valueOf(attendanceOverview.get("context"));
        int lateOrEarly = intValue(attendanceOverview.get("late"))
                + intValue(attendanceOverview.get("leaveEarly"));
        return List.of(
                new AiDashboard.Ratio(
                        "actual_working", "Thực tế có làm",
                        intValue(attendanceOverview.get("actualWorkingHeadcount")), total, unit,
                        "người thực tế làm", "người theo lịch", context, "green"),
                new AiDashboard.Ratio(
                        "scheduled_working", "Đúng lịch có làm",
                        intValue(attendanceOverview.get("scheduledWorkingHeadcount")), total, unit,
                        "người theo lịch có làm", "người theo lịch", context, "blue"),
                new AiDashboard.Ratio(
                        "absent", "Vắng mặt",
                        intValue(attendanceOverview.get("absent")), total, unit,
                        "người vắng", "người theo lịch", context, "red"),
                new AiDashboard.Ratio(
                        "on_leave", "Nghỉ phép",
                        intValue(attendanceOverview.get("onLeave")), total, unit,
                        "người nghỉ phép", "người theo lịch", context, "purple"),
                new AiDashboard.Ratio(
                        "late_or_early", "Đi muộn / về sớm",
                        lateOrEarly, total, unit,
                        "lượt bất thường", "người theo lịch", context, "orange"),
                new AiDashboard.Ratio(
                        "overtime", "Có tăng ca",
                        intValue(attendanceOverview.get("overtime")), total, unit,
                        "người tăng ca", "người theo lịch", context, "teal"),
                new AiDashboard.Ratio(
                        "unconfirmed", "Chưa xác nhận",
                        intValue(attendanceOverview.get("unconfirmed")), total, unit,
                        "người chưa chấm công", "người theo lịch", context, "gray")
        );
    }

    private List<AiDashboard.Bar> bars(DetailType type, List<Map<String, Object>> rows) {
        String labelKey = switch (type) {
            case MACHINE_DOWNTIME -> "displayName";
            case ATTENDANCE_EXCEPTIONS -> "attendanceStatusLabel";
            case QUALITY_ERRORS -> "errorTypeName";
            case MATERIAL_ISSUES -> "materialName";
        };
        String valueKey = switch (type) {
            case MACHINE_DOWNTIME -> "durationMinutes";
            case QUALITY_ERRORS -> "quantity";
            default -> null;
        };
        String unit = switch (type) {
            case MACHINE_DOWNTIME -> "phút";
            case ATTENDANCE_EXCEPTIONS -> "lượt";
            case QUALITY_ERRORS -> "sản phẩm";
            case MATERIAL_ISSUES -> "sự cố";
        };
        Map<String, BigDecimal> grouped = new LinkedHashMap<>();
        for (Map<String, Object> row : rows) {
            String label = String.valueOf(row.getOrDefault(labelKey, "Chưa xác định"));
            BigDecimal value = BigDecimal.ONE;
            if (valueKey != null && row.get(valueKey) instanceof Number number) {
                value = new BigDecimal(number.toString());
            }
            grouped.merge(label, value, BigDecimal::add);
        }
        return grouped.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .limit(8)
                .map(entry -> new AiDashboard.Bar(entry.getKey(), entry.getValue(), unit))
                .toList();
    }

    private List<AiDashboard.Column> columns(DetailType type) {
        List<AiDashboard.Column> common = new ArrayList<>(List.of(
                new AiDashboard.Column("reportDate", "Ngày", "date"),
                new AiDashboard.Column("shift", "Ca", "text"),
                new AiDashboard.Column("team", "Tổ", "text")
        ));
        switch (type) {
            case MACHINE_DOWNTIME -> common.addAll(List.of(
                    new AiDashboard.Column("displayName", "Máy", "text"),
                    new AiDashboard.Column("reason", "Nguyên nhân", "text"),
                    new AiDashboard.Column("durationMinutes", "Số phút", "number")));
            case ATTENDANCE_EXCEPTIONS -> common.addAll(List.of(
                    new AiDashboard.Column("displayName", "Nhân viên", "text"),
                    new AiDashboard.Column("attendanceStatusLabel", "Tình trạng", "text"),
                    new AiDashboard.Column("workingMinutes", "Phút làm", "number"),
                    new AiDashboard.Column("description", "Ghi chú", "text")));
            case QUALITY_ERRORS -> common.addAll(List.of(
                    new AiDashboard.Column("displayName", "Loại lỗi", "text"),
                    new AiDashboard.Column("severity", "Mức độ", "text"),
                    new AiDashboard.Column("quantity", "Số lượng", "number")));
            case MATERIAL_ISSUES -> common.addAll(List.of(
                    new AiDashboard.Column("displayName", "Vật tư", "text"),
                    new AiDashboard.Column("issueType", "Sự cố", "text"),
                    new AiDashboard.Column("quantity", "Số lượng", "number"),
                    new AiDashboard.Column("unit", "Đơn vị", "text")));
        }
        common.add(new AiDashboard.Column("recordSource", "Bảng nguồn", "status"));
        common.add(new AiDashboard.Column("dataStatus", "Nguồn", "status"));
        return common;
    }

    private String dashboardTitle(DetailType type) {
        return switch (type) {
            case MACHINE_DOWNTIME -> "Dashboard máy dừng và sự cố";
            case ATTENDANCE_EXCEPTIONS -> "Dashboard nhân sự thực tế theo ca";
            case QUALITY_ERRORS -> "Dashboard lỗi chất lượng";
            case MATERIAL_ISSUES -> "Dashboard sự cố vật tư";
        };
    }

    private String sourceType(DetailType type, boolean temporary) {
        return switch (type) {
            case MACHINE_DOWNTIME -> temporary ? "MACHINE_DOWNTIME_STAGING" : "MACHINE_DOWNTIME";
            case ATTENDANCE_EXCEPTIONS -> temporary ? "EMPLOYEE_ACTUAL_STAGING" : "EMPLOYEE_ACTUAL";
            case QUALITY_ERRORS -> temporary ? "QUALITY_REPORT_STAGING" : "QUALITY_REPORT";
            case MATERIAL_ISSUES -> temporary ? "MATERIAL_ISSUE_STAGING" : "MATERIAL_ISSUE";
        };
    }

    private String sourceLabel(DetailType type, boolean temporary) {
        String label = switch (type) {
            case MACHINE_DOWNTIME -> "Chi tiết dừng máy";
            case ATTENDANCE_EXCEPTIONS -> "Nhân sự thực tế trong ca";
            case QUALITY_ERRORS -> "Chi tiết lỗi chất lượng";
            case MATERIAL_ISSUES -> "Chi tiết sự cố vật tư";
        };
        return temporary ? label + " tạm/chưa xác nhận" : label + " chính thức";
    }

    private String attendanceLabel(AttendanceStatus status) {
        return switch (status) {
            case PRESENT -> "Có mặt";
            case ABSENT -> "Vắng mặt";
            case LATE -> "Đi muộn";
            case LEAVE_EARLY -> "Về sớm";
            case ON_LEAVE -> "Nghỉ phép";
        };
    }
}
