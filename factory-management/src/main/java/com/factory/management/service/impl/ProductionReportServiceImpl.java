package com.factory.management.service.impl;

import com.factory.management.dto.response.ProductionDashboardResponse;
import com.factory.management.dto.response.ProductionReportResponse;
import com.factory.management.entity.Department;
import com.factory.management.entity.Employee;
import com.factory.management.entity.EmployeeActual;
import com.factory.management.entity.EmployeeActualStaging;
import com.factory.management.entity.Factory;
import com.factory.management.entity.MachineDowntime;
import com.factory.management.entity.MachineDowntimeStaging;
import com.factory.management.entity.MaterialIssue;
import com.factory.management.entity.MaterialIssueStaging;
import com.factory.management.entity.ProductionLine;
import com.factory.management.entity.ProductionReport;
import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.QualityReport;
import com.factory.management.entity.QualityReportStaging;
import com.factory.management.entity.Role;
import com.factory.management.entity.Team;
import com.factory.management.entity.User;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.ProductionReportMapper;
import com.factory.management.repository.EmployeeActualRepository;
import com.factory.management.repository.EmployeeActualStagingRepository;
import com.factory.management.repository.MachineDowntimeRepository;
import com.factory.management.repository.MachineDowntimeStagingRepository;
import com.factory.management.repository.MaterialIssueRepository;
import com.factory.management.repository.MaterialIssueStagingRepository;
import com.factory.management.repository.ProductionReportRepository;
import com.factory.management.repository.QualityReportRepository;
import com.factory.management.repository.QualityReportStagingRepository;
import com.factory.management.repository.UserDataScopeRepository;
import com.factory.management.security.AuthorizationScope;
import com.factory.management.service.CurrentUserService;
import com.factory.management.service.ProductionReportService;
import jakarta.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Override
    @Transactional
    public ProductionReportResponse createFromApprovedStaging(
            ProductionReportStaging staging,
            Employee approver,
            String remark
    ) {
        if (repository.existsBySourceStaging_Id(staging.getId())) {
            throw new AppException(ErrorCode.PRODUCTION_REPORT_ALREADY_CREATED);
        }

        BigDecimal availability = percent(
                staging.getWorkingMinutes() - staging.getDowntimeMinutes(),
                staging.getWorkingMinutes()
        );
        BigDecimal performance = percent(staging.getActualQuantity(), staging.getPlannedQuantity());
        BigDecimal quality = percent(staging.getGoodQuantity(), staging.getActualQuantity());
        BigDecimal oee = availability.multiply(performance).multiply(quality)
                .divide(new BigDecimal("10000"), 2, RoundingMode.HALF_UP);
        LocalDateTime now = LocalDateTime.now();

        ProductionReport report = ProductionReport.builder()
                .reportNo(nextReportNo(staging.getReportDate()))
                .reportDate(staging.getReportDate())
                .sourceStaging(staging)
                .shift(staging.getShift())
                .factory(staging.getFactory())
                .department(staging.getDepartment())
                .productionLine(staging.getProductionLine())
                .team(staging.getTeam())
                .leaderEmployee(staging.getLeaderEmployee())
                .machine(staging.getMachine())
                .plannedQuantity(staging.getPlannedQuantity())
                .actualQuantity(staging.getActualQuantity())
                .goodQuantity(staging.getGoodQuantity())
                .defectQuantity(staging.getDefectQuantity())
                .workingMinutes(staging.getWorkingMinutes())
                .downtimeMinutes(staging.getDowntimeMinutes())
                .availability(availability)
                .performance(performance)
                .quality(quality)
                .oee(oee)
                .remark(trimToNull(remark))
                .approvedAt(now)
                .approvedBy(approver)
                .createdAt(now)
                .createdBy(staging.getLeaderEmployee())
                .build();

        report = repository.saveAndFlush(report);
        copyOfficialDetails(staging.getId(), report);
        return mapper.mapToResponse(report);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionReportResponse> getAll() {
        return map(accessible(repository.findAll()));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionReportResponse getById(Long id) {
        return mapper.mapToResponse(requireAccessible(find(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionReportResponse getByReportNo(String no) {
        ProductionReport report = repository.findByReportNo(no)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_REPORT_NOT_FOUND));
        return mapper.mapToResponse(requireAccessible(report));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionReportResponse> getByDate(LocalDate date) {
        return map(accessible(repository.findAllByReportDate(date)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionReportResponse> getByTeamId(Long id) {
        return map(accessible(repository.findAllByTeam_Id(id)));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionReportResponse> getByMachineId(Long id) {
        return map(accessible(repository.findAllByMachine_Id(id)));
    }

    @Override
    @Transactional(readOnly = true)
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
            List<Predicate> predicates = new ArrayList<>();
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

            query.orderBy(
                    criteriaBuilder.desc(root.get("reportDate")),
                    criteriaBuilder.desc(root.get("id"))
            );
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        })));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionDashboardResponse getDashboard() {
        return dashboard(accessible(repository.findAll()), "AUTHORIZED", null, "Phạm vi được phép xem");
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionDashboardResponse getMyDashboard() {
        User user = currentUserService.user();
        Employee employee = user.getEmployee();
        Set<Role> roles = user.getRoles();
        if (roles.contains(Role.ADMIN) || roles.contains(Role.DIRECTOR)) {
            return dashboard(repository.findAll(), "COMPANY", null, "Toàn công ty");
        }

        var explicitScopes = userDataScopeRepository.findAllByUser_Id(user.getId());
        List<ProductionReport> visibleReports = accessible(repository.findAll());
        boolean pureFinance = roles.contains(Role.FINANCE)
                && roles.stream().noneMatch(role -> role == Role.FACTORY_MANAGER
                || role == Role.DEPARTMENT_MANAGER
                || role == Role.PRODUCTION_MANAGER
                || role == Role.TEAM_LEADER);

        if (pureFinance) {
            if (explicitScopes.isEmpty()) {
                return dashboard(List.of(), "NO_SCOPE", null, "Chưa được cấp phạm vi dữ liệu");
            }
            return dashboard(
                    visibleReports,
                    explicitScopes.size() == 1 ? explicitScopes.get(0).getScopeType().name() : "MULTI_SCOPE",
                    explicitScopes.size() == 1 ? explicitScopes.get(0).getScopeId() : null,
                    "Phạm vi dữ liệu được cấp"
            );
        }

        if (!explicitScopes.isEmpty()) {
            return dashboard(
                    visibleReports,
                    explicitScopes.size() == 1 ? explicitScopes.get(0).getScopeType().name() : "MULTI_SCOPE",
                    explicitScopes.size() == 1 ? explicitScopes.get(0).getScopeId() : null,
                    "Phạm vi dữ liệu được cấp"
            );
        }

        Team team = employee.getTeam();
        if (team == null) {
            return dashboard(List.of(), "UNASSIGNED", employee.getId(), "Chưa được phân đơn vị");
        }

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
        if (roles.contains(Role.TEAM_LEADER)) {
            return dashboard(visibleReports, "TEAM", team.getId(), team.getName());
        }

        return dashboard(List.of(), "PERSONAL", employee.getId(), "Dữ liệu cá nhân");
    }

    private ProductionDashboardResponse dashboard(
            List<ProductionReport> reports,
            String scopeType,
            Long scopeId,
            String scopeName
    ) {
        long count = reports.size();
        return ProductionDashboardResponse.builder()
                .reportCount(count)
                .plannedQuantity(reports.stream().mapToLong(ProductionReport::getPlannedQuantity).sum())
                .actualQuantity(reports.stream().mapToLong(ProductionReport::getActualQuantity).sum())
                .goodQuantity(reports.stream().mapToLong(ProductionReport::getGoodQuantity).sum())
                .defectQuantity(reports.stream().mapToLong(ProductionReport::getDefectQuantity).sum())
                .downtimeMinutes(reports.stream().mapToLong(ProductionReport::getDowntimeMinutes).sum())
                .averageAvailability(average(reports.stream().map(ProductionReport::getAvailability).toList()))
                .averagePerformance(average(reports.stream().map(ProductionReport::getPerformance).toList()))
                .averageQuality(average(reports.stream().map(ProductionReport::getQuality).toList()))
                .averageOee(average(reports.stream().map(ProductionReport::getOee).toList()))
                .scopeType(scopeType)
                .scopeId(scopeId)
                .scopeName(scopeName)
                .build();
    }

    private BigDecimal percent(long numerator, long denominator) {
        if (denominator <= 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return BigDecimal.valueOf(numerator)
                .multiply(ONE_HUNDRED)
                .divide(BigDecimal.valueOf(denominator), 2, RoundingMode.HALF_UP)
                .min(ONE_HUNDRED.setScale(2));
    }

    private void copyOfficialDetails(Long stagingId, ProductionReport report) {
        machineDowntimeRepository.saveAll(
                machineDowntimeStagingRepository.findAllByProductionReportStaging_IdAndActiveTrue(stagingId)
                        .stream()
                        .map(value -> toOfficialMachineDowntime(value, report))
                        .toList()
        );
        qualityReportRepository.saveAll(
                qualityReportStagingRepository.findAllByProductionReportStaging_IdAndActiveTrue(stagingId)
                        .stream()
                        .map(value -> toOfficialQualityReport(value, report))
                        .toList()
        );
        materialIssueRepository.saveAll(
                materialIssueStagingRepository.findAllByProductionReportStaging_IdAndActiveTrue(stagingId)
                        .stream()
                        .map(value -> toOfficialMaterialIssue(value, report))
                        .toList()
        );
        employeeActualRepository.saveAll(
                employeeActualStagingRepository.findAllByProductionReportStaging_IdAndActiveTrue(stagingId)
                        .stream()
                        .map(value -> toOfficialEmployeeActual(value, report))
                        .toList()
        );
    }

    private MachineDowntime toOfficialMachineDowntime(
            MachineDowntimeStaging value,
            ProductionReport report
    ) {
        return MachineDowntime.builder()
                .productionReport(report)
                .sourceStaging(value)
                .machine(value.getMachine())
                .downtimeReason(value.getDowntimeReason())
                .startTime(value.getStartTime())
                .endTime(value.getEndTime())
                .durationMinutes(value.getDurationMinutes())
                .description(value.getDescription())
                .build();
    }

    private QualityReport toOfficialQualityReport(
            QualityReportStaging value,
            ProductionReport report
    ) {
        return QualityReport.builder()
                .productionReport(report)
                .sourceStaging(value)
                .qualityErrorType(value.getQualityErrorType())
                .quantity(value.getQuantity())
                .description(value.getDescription())
                .build();
    }

    private MaterialIssue toOfficialMaterialIssue(
            MaterialIssueStaging value,
            ProductionReport report
    ) {
        return MaterialIssue.builder()
                .productionReport(report)
                .sourceStaging(value)
                .material(value.getMaterial())
                .issueType(value.getIssueType())
                .quantity(value.getQuantity())
                .unit(value.getUnit())
                .description(value.getDescription())
                .build();
    }

    private EmployeeActual toOfficialEmployeeActual(
            EmployeeActualStaging value,
            ProductionReport report
    ) {
        return EmployeeActual.builder()
                .productionReport(report)
                .sourceStaging(value)
                .employee(value.getEmployee())
                .workingMinutes(value.getWorkingMinutes())
                .overtimeMinutes(value.getOvertimeMinutes())
                .attendanceStatus(value.getAttendanceStatus())
                .assignmentType(value.getAssignmentType())
                .description(value.getDescription())
                .build();
    }

    private BigDecimal average(List<BigDecimal> values) {
        if (values.isEmpty()) {
            return BigDecimal.ZERO.setScale(2);
        }
        return values.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(values.size()), 2, RoundingMode.HALF_UP);
    }

    private synchronized String nextReportNo(LocalDate date) {
        long sequence = repository.countByReportDate(date) + 1;
        return "PR" + date.format(DateTimeFormatter.BASIC_ISO_DATE) + String.format("%04d", sequence);
    }

    private ProductionReport find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_REPORT_NOT_FOUND));
    }

    private List<ProductionReport> accessible(List<ProductionReport> reports) {
        return reports.stream()
                .filter(authorizationScope::canAccessReportRecord)
                .toList();
    }

    private ProductionReport requireAccessible(ProductionReport report) {
        if (!authorizationScope.canAccessReportRecord(report)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        return report;
    }

    private List<ProductionReportResponse> map(List<ProductionReport> list) {
        return list.stream()
                .map(mapper::mapToResponse)
                .toList();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
