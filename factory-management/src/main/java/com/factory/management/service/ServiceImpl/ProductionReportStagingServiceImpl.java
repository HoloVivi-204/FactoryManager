package com.factory.management.service.ServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.factory.management.dto.request.ProductionReportApprovalRequest;
import com.factory.management.dto.request.ProductionReportStagingRequest;
import com.factory.management.dto.request.ProductionReportStagingUpdateRequest;
import com.factory.management.dto.request.ProductionReportReviewRequest;
import com.factory.management.dto.response.ProductionReportResponse;
import com.factory.management.dto.response.ProductionReportStagingResponse;
import com.factory.management.entity.Department;
import com.factory.management.entity.Employee;
import com.factory.management.entity.Factory;
import com.factory.management.entity.Machine;
import com.factory.management.entity.ProductionLine;
import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.ProductionReportStatus;
import com.factory.management.entity.Shift;
import com.factory.management.entity.Team;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.ProductionReportStagingMapper;
import com.factory.management.repository.DepartmentRepository;
import com.factory.management.repository.EmployeeRepository;
import com.factory.management.repository.FactoryRepository;
import com.factory.management.repository.MachineRepository;
import com.factory.management.repository.MachineDowntimeStagingRepository;
import com.factory.management.repository.MaterialIssueStagingRepository;
import com.factory.management.repository.EmployeeActualStagingRepository;
import com.factory.management.repository.ProductionLineRepository;
import com.factory.management.repository.ProductionReportStagingRepository;
import com.factory.management.repository.QualityReportStagingRepository;
import com.factory.management.repository.ShiftRepository;
import com.factory.management.repository.TeamRepository;
import com.factory.management.security.AuthorizationScope;
import com.factory.management.service.Service.ProductionReportService;
import com.factory.management.service.Service.ProductionReportStagingService;
import com.factory.management.service.Service.CurrentUserService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductionReportStagingServiceImpl implements ProductionReportStagingService {
    ProductionReportStagingRepository reportRepository;
    ShiftRepository shiftRepository;
    FactoryRepository factoryRepository;
    DepartmentRepository departmentRepository;
    ProductionLineRepository productionLineRepository;
    TeamRepository teamRepository;
    EmployeeRepository employeeRepository;
    MachineRepository machineRepository;
    QualityReportStagingRepository qualityReportStagingRepository;
    MachineDowntimeStagingRepository machineDowntimeStagingRepository;
    MaterialIssueStagingRepository materialIssueStagingRepository;
    EmployeeActualStagingRepository employeeActualStagingRepository;
    ProductionReportService productionReportService;
    CurrentUserService currentUserService;
    AuthorizationScope authorizationScope;
    AuditService auditService;
    ProductionReportStagingMapper mapper;

    @Override
    @Transactional
    public ProductionReportStagingResponse create(ProductionReportStagingRequest r) {
        Relations x = relations(r.getShiftId(), r.getFactoryId(), r.getDepartmentId(),
                r.getProductionLineId(), r.getTeamId(), r.getLeaderEmployeeId(), r.getMachineId());
        validateNumbers(r.getActualQuantity(), r.getDefectQuantity(), r.getWorkingMinutes(), r.getDowntimeMinutes());
        ensureUnique(r.getReportDate(), r.getShiftId(), r.getTeamId(), r.getMachineId(), null);

        ProductionReportStaging report = ProductionReportStaging.builder()
                .reportDate(r.getReportDate()).shift(x.shift).factory(x.factory)
                .department(x.department).productionLine(x.line).team(x.team)
                .leaderEmployee(x.leader).machine(x.machine)
                .plannedQuantity(r.getPlannedQuantity()).actualQuantity(r.getActualQuantity())
                .defectQuantity(r.getDefectQuantity())
                .goodQuantity(r.getActualQuantity() - r.getDefectQuantity())
                .workingMinutes(r.getWorkingMinutes()).downtimeMinutes(r.getDowntimeMinutes())
                .note(trimToNull(r.getNote())).status(ProductionReportStatus.DRAFT).build();
        return mapper.mapToResponse(save(report));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionReportStagingResponse> getAll() {
        return map(reportRepository.findAll());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionReportStagingResponse> getMyScope() {
        var teamIds = authorizationScope.accessibleTeamIds();
        if (teamIds.isEmpty()) return List.of();
        return map(reportRepository.findAllByTeam_IdInOrderByReportDateDescIdDesc(teamIds));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionReportStagingResponse> getByDate(LocalDate date) {
        return map(reportRepository.findAllByReportDate(date));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionReportStagingResponse> getByTeamId(Long id) {
        return map(reportRepository.findAllByTeam_Id(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionReportStagingResponse> getByMachineId(Long id) {
        return map(reportRepository.findAllByMachine_Id(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductionReportStagingResponse> getByStatus(ProductionReportStatus status) {
        return map(reportRepository.findAllByStatus(status));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductionReportStagingResponse getById(Long id) {
        return mapper.mapToResponse(find(id));
    }

    @Override
    @Transactional
    public ProductionReportStagingResponse update(Long id, ProductionReportStagingUpdateRequest r) {
        ProductionReportStaging p = find(id);
        requireStatus(p, ProductionReportStatus.DRAFT);
        LocalDate date = r.getReportDate() == null ? p.getReportDate() : r.getReportDate();
        Long shiftId = value(r.getShiftId(), p.getShift().getId());
        Long factoryId = value(r.getFactoryId(), p.getFactory().getId());
        Long departmentId = value(r.getDepartmentId(), p.getDepartment().getId());
        Long lineId = value(r.getProductionLineId(), p.getProductionLine().getId());
        Long teamId = value(r.getTeamId(), p.getTeam().getId());
        Long leaderId = value(r.getLeaderEmployeeId(), p.getLeaderEmployee().getId());
        Long machineId = value(r.getMachineId(), p.getMachine().getId());
        Relations x = relations(shiftId, factoryId, departmentId, lineId, teamId, leaderId, machineId);
        Long planned = value(r.getPlannedQuantity(), p.getPlannedQuantity());
        Long actual = value(r.getActualQuantity(), p.getActualQuantity());
        Long defect = value(r.getDefectQuantity(), p.getDefectQuantity());
        Integer working = value(r.getWorkingMinutes(), p.getWorkingMinutes());
        Integer downtime = value(r.getDowntimeMinutes(), p.getDowntimeMinutes());
        validateNumbers(actual, defect, working, downtime);
        if (qualityReportStagingRepository.sumActiveQuantityByReportId(id) > defect)
            throw new AppException(ErrorCode.QUALITY_TOTAL_EXCEEDS_REPORT_DEFECT);
        ensureUnique(date, shiftId, teamId, machineId, id);

        p.setReportDate(date);
        p.setShift(x.shift);
        p.setFactory(x.factory);
        p.setDepartment(x.department);
        p.setProductionLine(x.line);
        p.setTeam(x.team);
        p.setLeaderEmployee(x.leader);
        p.setMachine(x.machine);
        p.setPlannedQuantity(planned);
        p.setActualQuantity(actual);
        p.setDefectQuantity(defect);
        p.setGoodQuantity(actual - defect);
        p.setWorkingMinutes(working);
        p.setDowntimeMinutes(downtime);
        if (r.getNote() != null)
            p.setNote(trimToNull(r.getNote()));
        return mapper.mapToResponse(save(p));
    }

    @Override
    @Transactional
    public ProductionReportStagingResponse submit(Long id) {
        ProductionReportStaging p = find(id);
        requireStatus(p, ProductionReportStatus.DRAFT);
        validateBeforeSubmit(p);
        p.setStatus(ProductionReportStatus.SUBMITTED);
        p.setSubmittedAt(LocalDateTime.now());
        p.setReviewComment(null);
        return mapper.mapToResponse(p);
    }

    @Override
    @Transactional
    public ProductionReportStagingResponse requestChange(Long id, ProductionReportReviewRequest request) {
        ProductionReportStaging p = find(id);
        requireStatus(p, ProductionReportStatus.SUBMITTED);
        p.setStatus(ProductionReportStatus.CHANGE_REQUESTED);
        p.setReviewedAt(LocalDateTime.now());
        p.setReviewComment(request.getComment().trim());
        return mapper.mapToResponse(p);
    }

    @Override
    @Transactional
    public ProductionReportStagingResponse returnToDraft(Long id) {
        return transition(id, ProductionReportStatus.CHANGE_REQUESTED, ProductionReportStatus.DRAFT);
    }

    @Override
    @Transactional
    public ProductionReportResponse approve(Long id, ProductionReportApprovalRequest request) {
        ProductionReportStaging report = find(id);
        requireStatus(report, ProductionReportStatus.SUBMITTED);
        Employee approver = currentUserService.employee();
        report.setStatus(ProductionReportStatus.APPROVED);
        report.setReviewedAt(LocalDateTime.now());
        report.setReviewComment(trimToNull(request.getRemark()));
        return productionReportService.createFromApprovedStaging(report, approver, request.getRemark());
    }

    @Override
    @Transactional
    public ProductionReportStagingResponse lock(Long id) {
        return transition(id, ProductionReportStatus.APPROVED, ProductionReportStatus.LOCKED);
    }

    @Override
    @Transactional
    public void deleteDraft(Long id) {
        ProductionReportStaging report = find(id);
        if (report.getStatus() != ProductionReportStatus.DRAFT)
            throw new AppException(ErrorCode.STAGING_REPORT_DELETE_REQUIRES_DRAFT);

        machineDowntimeStagingRepository.deleteAllByProductionReportStaging_Id(id);
        qualityReportStagingRepository.deleteAllByProductionReportStaging_Id(id);
        materialIssueStagingRepository.deleteAllByProductionReportStaging_Id(id);
        employeeActualStagingRepository.deleteAllByProductionReportStaging_Id(id);
        reportRepository.delete(report);
        auditService.record("DELETE_DRAFT", "ProductionReportStaging", id,
                "{\"reportDate\":\"" + report.getReportDate() + "\",\"teamId\":"
                        + report.getTeam().getId() + ",\"machineId\":" + report.getMachine().getId() + "}");
    }

    private ProductionReportStagingResponse transition(Long id, ProductionReportStatus from,
            ProductionReportStatus to) {
        ProductionReportStaging p = find(id);
        requireStatus(p, from);
        p.setStatus(to);
        return mapper.mapToResponse(p);
    }

    private Relations relations(Long shiftId, Long factoryId, Long departmentId, Long lineId, Long teamId,
            Long leaderId, Long machineId) {
        Shift shift = shiftRepository.findByIdAndActiveTrue(shiftId)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_ID_NOT_FOUND));
        Factory factory = factoryRepository.findByIdAndActiveTrue(factoryId)
                .orElseThrow(() -> new AppException(ErrorCode.FACTORY_ID_NOT_FOUND));
        Department department = departmentRepository.findByIdAndActiveTrueAndFactory_ActiveTrue(departmentId)
                .orElseThrow(() -> new AppException(ErrorCode.DEPARTMENT_ID_NOT_FOUND));
        ProductionLine line = productionLineRepository
                .findByIdAndActiveTrueAndDepartment_ActiveTrueAndDepartment_Factory_ActiveTrue(lineId)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_LINE_ID_NOT_FOUND));
        Team team = teamRepository
                .findByIdAndActiveTrueAndProductionLine_ActiveTrueAndProductionLine_Department_ActiveTrueAndProductionLine_Department_Factory_ActiveTrue(
                        teamId)
                .orElseThrow(() -> new AppException(ErrorCode.TEAM_ID_NOT_FOUND));
        Employee leader = employeeRepository
                .findByIdAndActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue(
                        leaderId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ID_NOT_FOUND));
        Machine machine = machineRepository
                .findByIdAndActiveTrueAndMachineType_ActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue(
                        machineId)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_ID_NOT_FOUND));
        if (!department.getFactory().getId().equals(factoryId) || !line.getDepartment().getId().equals(departmentId)
                || !team.getProductionLine().getId().equals(lineId) || !leader.getTeam().getId().equals(teamId)
                || !machine.getTeam().getId().equals(teamId))
            throw new AppException(ErrorCode.INVALID_REPORT_ORGANIZATION);
        return new Relations(shift, factory, department, line, team, leader, machine);
    }

    private void validateNumbers(Long actual, Long defect, Integer working, Integer downtime) {
        if (defect > actual)
            throw new AppException(ErrorCode.REPORT_DEFECT_EXCEEDS_ACTUAL);
        if (downtime > working)
            throw new AppException(ErrorCode.REPORT_DOWNTIME_EXCEEDS_WORKING);
    }

    private void validateBeforeSubmit(ProductionReportStaging report) {
        validateNumbers(report.getActualQuantity(), report.getDefectQuantity(), report.getWorkingMinutes(), report.getDowntimeMinutes());
        long qualityTotal = qualityReportStagingRepository.sumActiveQuantityByReportId(report.getId());
        if (qualityTotal > report.getDefectQuantity())
            throw new AppException(ErrorCode.QUALITY_TOTAL_EXCEEDS_REPORT_DEFECT);
    }

    private void ensureUnique(LocalDate d, Long s, Long t, Long m, Long id) {
        boolean exists = id == null ? reportRepository.existsByReportDateAndShift_IdAndTeam_IdAndMachine_Id(d, s, t, m)
                : reportRepository.existsByReportDateAndShift_IdAndTeam_IdAndMachine_IdAndIdNot(d, s, t, m, id);
        if (exists)
            throw new AppException(ErrorCode.PRODUCTION_REPORT_STAGING_EXISTS);
    }

    private void requireStatus(ProductionReportStaging p, ProductionReportStatus expected) {
        if (p.getStatus() != expected)
            throw new AppException(ErrorCode.INVALID_REPORT_STATUS_TRANSITION);
    }

    private ProductionReportStaging find(Long id) {
        return reportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_REPORT_STAGING_NOT_FOUND));
    }

    private ProductionReportStaging save(ProductionReportStaging p) {
        try {
            return reportRepository.saveAndFlush(p);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.PRODUCTION_REPORT_STAGING_EXISTS);
        }
    }

    private List<ProductionReportStagingResponse> map(List<ProductionReportStaging> list) {
        return list.stream().map(mapper::mapToResponse).toList();
    }

    private String trimToNull(String s) {
        return s == null || s.isBlank() ? null : s.trim();
    }

    private <T> T value(T requested, T current) {
        return requested == null ? current : requested;
    }

    private record Relations(Shift shift, Factory factory, Department department, ProductionLine line, Team team,
            Employee leader, Machine machine) {
    }
}
