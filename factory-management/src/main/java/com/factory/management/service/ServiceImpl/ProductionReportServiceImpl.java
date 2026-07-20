package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.response.*;
import com.factory.management.entity.*;
import com.factory.management.exception.*;
import com.factory.management.mapper.ProductionReportMapper;
import com.factory.management.repository.*;
import com.factory.management.security.AuthorizationScope;
import com.factory.management.service.Service.ProductionReportService;
import com.factory.management.service.Service.CurrentUserService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductionReportServiceImpl implements ProductionReportService {
    static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    ProductionReportRepository repository;
    MachineDowntimeStagingRepository machineDowntimeStagingRepository;
    QualityReportStagingRepository qualityReportStagingRepository;
    MaterialIssueStagingRepository materialIssueStagingRepository;
    EmployeeActualStagingRepository employeeActualStagingRepository;
    MachineDowntimeRepository machineDowntimeRepository;
    QualityReportRepository qualityReportRepository;
    MaterialIssueRepository materialIssueRepository;
    EmployeeActualRepository employeeActualRepository;
    UserDataScopeRepository userDataScopeRepository;
    CurrentUserService currentUserService;
    AuthorizationScope authorizationScope;
    ProductionReportMapper mapper;

    @Override @Transactional
    public ProductionReportResponse createFromApprovedStaging(ProductionReportStaging s, Employee approver, String remark) {
        if (repository.existsBySourceStaging_Id(s.getId()))
            throw new AppException(ErrorCode.PRODUCTION_REPORT_ALREADY_CREATED);
        BigDecimal availability = percent(s.getWorkingMinutes() - s.getDowntimeMinutes(), s.getWorkingMinutes());
        BigDecimal performance = percent(s.getActualQuantity(), s.getPlannedQuantity());
        BigDecimal quality = percent(s.getGoodQuantity(), s.getActualQuantity());
        BigDecimal oee = availability.multiply(performance).multiply(quality)
                .divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP);
        LocalDateTime now = LocalDateTime.now();
        ProductionReport report = ProductionReport.builder()
                .reportNo(nextReportNo(s.getReportDate())).reportDate(s.getReportDate()).sourceStaging(s)
                .shift(s.getShift()).factory(s.getFactory()).department(s.getDepartment())
                .productionLine(s.getProductionLine()).team(s.getTeam())
                .leaderEmployee(s.getLeaderEmployee()).machine(s.getMachine())
                .plannedQuantity(s.getPlannedQuantity()).actualQuantity(s.getActualQuantity())
                .goodQuantity(s.getGoodQuantity()).defectQuantity(s.getDefectQuantity())
                .workingMinutes(s.getWorkingMinutes()).downtimeMinutes(s.getDowntimeMinutes())
                .availability(availability).performance(performance).quality(quality).oee(oee)
                .remark(trimToNull(remark)).approvedAt(now).approvedBy(approver)
                .createdAt(now).createdBy(s.getLeaderEmployee()).build();
        report = repository.saveAndFlush(report);
        copyOfficialDetails(s.getId(), report);
        return mapper.mapToResponse(report);
    }

    @Override @Transactional(readOnly = true) public List<ProductionReportResponse> getAll() { return map(accessible(repository.findAll())); }
    @Override @Transactional(readOnly = true) public ProductionReportResponse getById(Long id) { return mapper.mapToResponse(requireAccessible(find(id))); }
    @Override @Transactional(readOnly = true) public ProductionReportResponse getByReportNo(String no) { return mapper.mapToResponse(requireAccessible(repository.findByReportNo(no).orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_REPORT_NOT_FOUND)))); }
    @Override @Transactional(readOnly = true) public List<ProductionReportResponse> getByDate(LocalDate date) { return map(accessible(repository.findAllByReportDate(date))); }
    @Override @Transactional(readOnly = true) public List<ProductionReportResponse> getByTeamId(Long id) { return map(accessible(repository.findAllByTeam_Id(id))); }
    @Override @Transactional(readOnly = true) public List<ProductionReportResponse> getByMachineId(Long id) { return map(accessible(repository.findAllByMachine_Id(id))); }

    @Override @Transactional(readOnly = true)
    public List<ProductionReportResponse> search(
            LocalDate fromDate,
            LocalDate toDate,
            Long factoryId,
            Long teamId,
            Long machineId
    ) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            throw new AppException(ErrorCode.INVALID_PRODUCTION_REPORT_DATE_RANGE);
        }

        return map(accessible(repository.findAll((root, query, criteriaBuilder) -> {
            var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
            if (fromDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.<LocalDate>get("reportDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.<LocalDate>get("reportDate"), toDate));
            }
            if (factoryId != null) {
                predicates.add(criteriaBuilder.equal(root.get("factory").get("id"), factoryId));
            }
            if (teamId != null) {
                predicates.add(criteriaBuilder.equal(root.get("team").get("id"), teamId));
            }
            if (machineId != null) {
                predicates.add(criteriaBuilder.equal(root.get("machine").get("id"), machineId));
            }
            query.orderBy(criteriaBuilder.desc(root.get("reportDate")), criteriaBuilder.desc(root.get("id")));
            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        })));
    }

    @Override @Transactional(readOnly = true)
    public ProductionDashboardResponse getDashboard() {
        return dashboard(accessible(repository.findAll()), "AUTHORIZED", null, "Phạm vi được phép xem");
    }

    @Override @Transactional(readOnly = true)
    public ProductionDashboardResponse getMyDashboard() {
        User user = currentUserService.user();
        Employee employee = user.getEmployee();
        Set<Role> roles = user.getRoles();
        if (roles.contains(Role.ADMIN) || roles.contains(Role.DIRECTOR))
            return dashboard(repository.findAll(), "COMPANY", null, "Toàn công ty");

        var explicitScopes = userDataScopeRepository.findAllByUser_Id(user.getId());
        List<ProductionReport> visibleReports = accessible(repository.findAll());
        boolean pureFinance = roles.contains(Role.FINANCE)
                && roles.stream().noneMatch(role -> role == Role.FACTORY_MANAGER
                        || role == Role.DEPARTMENT_MANAGER
                        || role == Role.PRODUCTION_MANAGER
                        || role == Role.TEAM_LEADER);
        if (pureFinance) {
            if (explicitScopes.isEmpty())
                return dashboard(List.of(), "NO_SCOPE", null, "Chưa được cấp phạm vi dữ liệu");
            return dashboard(visibleReports,
                    explicitScopes.size() == 1 ? explicitScopes.get(0).getScopeType().name() : "MULTI_SCOPE",
                    explicitScopes.size() == 1 ? explicitScopes.get(0).getScopeId() : null,
                    "Phạm vi dữ liệu được cấp");
        }
        if (!explicitScopes.isEmpty())
            return dashboard(visibleReports,
                    explicitScopes.size() == 1 ? explicitScopes.get(0).getScopeType().name() : "MULTI_SCOPE",
                    explicitScopes.size() == 1 ? explicitScopes.get(0).getScopeId() : null,
                    "Phạm vi dữ liệu được cấp");

        Team team = employee.getTeam();
        if (team == null)
            return dashboard(List.of(), "UNASSIGNED", employee.getId(), "Chưa được phân đơn vị");

        if (roles.contains(Role.FACTORY_MANAGER)) {
            Factory factory = team.getProductionLine().getDepartment().getFactory();
            return dashboard(visibleReports, "FACTORY", factory.getId(), factory.getName());
        }
        if (roles.contains(Role.DEPARTMENT_MANAGER)) {
            Department department = team.getProductionLine().getDepartment();
            return dashboard(visibleReports, "DEPARTMENT", department.getId(), department.getName());
        }
        if (roles.contains(Role.PRODUCTION_MANAGER)) {
            ProductionLine line = team.getProductionLine();
            return dashboard(visibleReports, "PRODUCTION_LINE", line.getId(), line.getName());
        }
        if (roles.contains(Role.TEAM_LEADER))
            return dashboard(visibleReports, "TEAM", team.getId(), team.getName());
        return dashboard(List.of(), "PERSONAL", employee.getId(), "Dữ liệu cá nhân");
    }

    private ProductionDashboardResponse dashboard(List<ProductionReport> reports, String scopeType, Long scopeId, String scopeName) {
        long count = reports.size();
        return ProductionDashboardResponse.builder().reportCount(count)
                .plannedQuantity(reports.stream().mapToLong(ProductionReport::getPlannedQuantity).sum())
                .actualQuantity(reports.stream().mapToLong(ProductionReport::getActualQuantity).sum())
                .goodQuantity(reports.stream().mapToLong(ProductionReport::getGoodQuantity).sum())
                .defectQuantity(reports.stream().mapToLong(ProductionReport::getDefectQuantity).sum())
                .downtimeMinutes(reports.stream().mapToLong(ProductionReport::getDowntimeMinutes).sum())
                .averageAvailability(average(reports.stream().map(ProductionReport::getAvailability).toList()))
                .averagePerformance(average(reports.stream().map(ProductionReport::getPerformance).toList()))
                .averageQuality(average(reports.stream().map(ProductionReport::getQuality).toList()))
                .averageOee(average(reports.stream().map(ProductionReport::getOee).toList()))
                .scopeType(scopeType).scopeId(scopeId).scopeName(scopeName).build();
    }

    private BigDecimal percent(long numerator, long denominator) {
        if (denominator <= 0) return BigDecimal.ZERO.setScale(2);
        return BigDecimal.valueOf(numerator).multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP)
                .min(ONE_HUNDRED.setScale(2));
    }
    private void copyOfficialDetails(Long stagingId, ProductionReport report) {
        machineDowntimeRepository.saveAll(machineDowntimeStagingRepository.findAllByProductionReportStaging_IdAndActiveTrue(stagingId).stream()
                .map(x -> MachineDowntime.builder().productionReport(report).sourceStaging(x).machine(x.getMachine())
                        .downtimeReason(x.getDowntimeReason()).startTime(x.getStartTime()).endTime(x.getEndTime())
                        .durationMinutes(x.getDurationMinutes()).description(x.getDescription()).build()).toList());
        qualityReportRepository.saveAll(qualityReportStagingRepository.findAllByProductionReportStaging_IdAndActiveTrue(stagingId).stream()
                .map(x -> QualityReport.builder().productionReport(report).sourceStaging(x).qualityErrorType(x.getQualityErrorType())
                        .quantity(x.getQuantity()).description(x.getDescription()).build()).toList());
        materialIssueRepository.saveAll(materialIssueStagingRepository.findAllByProductionReportStaging_IdAndActiveTrue(stagingId).stream()
                .map(x -> MaterialIssue.builder().productionReport(report).sourceStaging(x).material(x.getMaterial())
                        .issueType(x.getIssueType()).quantity(x.getQuantity()).unit(x.getUnit()).description(x.getDescription()).build()).toList());
        employeeActualRepository.saveAll(employeeActualStagingRepository.findAllByProductionReportStaging_IdAndActiveTrue(stagingId).stream()
                .map(x -> EmployeeActual.builder().productionReport(report).sourceStaging(x).employee(x.getEmployee())
                        .workingMinutes(x.getWorkingMinutes()).overtimeMinutes(x.getOvertimeMinutes())
                        .attendanceStatus(x.getAttendanceStatus()).assignmentType(x.getAssignmentType()).description(x.getDescription()).build()).toList());
    }
    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) return BigDecimal.ZERO.setScale(2);
        return values.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }
    private synchronized String nextReportNo(LocalDate date) {
        long sequence = repository.countByReportDate(date) + 1;
        return "PR" + date.format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%04d", sequence);
    }
    private ProductionReport find(Long id) { return repository.findById(id).orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_REPORT_NOT_FOUND)); }
    private List<ProductionReport> accessible(List<ProductionReport> reports) {
        return reports.stream().filter(authorizationScope::canAccessReportRecord).toList();
    }
    private ProductionReport requireAccessible(ProductionReport report) {
        if (!authorizationScope.canAccessReportRecord(report)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        return report;
    }
    private List<ProductionReportResponse> map(List<ProductionReport> list) { return list.stream().map(mapper::mapToResponse).toList(); }
    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
