package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.*;
import com.factory.management.dto.response.EmployeeActualStagingResponse;
import com.factory.management.entity.*;
import com.factory.management.exception.*;
import com.factory.management.mapper.EmployeeActualStagingMapper;
import com.factory.management.repository.*;
import com.factory.management.service.Service.EmployeeActualStagingService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeActualStagingServiceImpl implements EmployeeActualStagingService {
    EmployeeActualStagingRepository repository;
    ProductionReportStagingRepository reportRepository;
    EmployeeRepository employeeRepository;
    EmployeeActualStagingMapper mapper;

    @Override @Transactional
    public EmployeeActualStagingResponse create(EmployeeActualStagingRequest r) {
        ProductionReportStaging report = editableReport(r.getProductionReportStagingId());
        Employee employee = activeEmployee(r.getEmployeeId());
        ensureUnique(report.getId(), employee.getId(), null);
        validateAttendance(r.getAttendanceStatus(), r.getWorkingMinutes());
        EmployeeActualStaging value = EmployeeActualStaging.builder()
                .productionReportStaging(report).employee(employee)
                .workingMinutes(r.getWorkingMinutes()).overtimeMinutes(r.getOvertimeMinutes())
                .attendanceStatus(r.getAttendanceStatus()).assignmentType(r.getAssignmentType())
                .description(trimToNull(r.getDescription()))
                .active(r.getActive() == null ? true : r.getActive()).build();
        return mapper.mapToResponse(save(value));
    }

    @Override @Transactional(readOnly = true)
    public List<EmployeeActualStagingResponse> getAll() { return map(repository.findAllByActiveTrue()); }
    @Override @Transactional(readOnly = true)
    public List<EmployeeActualStagingResponse> getByReportId(Long id) { return map(repository.findAllByProductionReportStaging_IdAndActiveTrue(id)); }
    @Override @Transactional(readOnly = true)
    public List<EmployeeActualStagingResponse> getByEmployeeId(Long id) { return map(repository.findAllByEmployee_IdAndActiveTrue(id)); }
    @Override @Transactional(readOnly = true)
    public EmployeeActualStagingResponse getById(Long id) { return mapper.mapToResponse(activeValue(id)); }

    @Override @Transactional
    public EmployeeActualStagingResponse update(Long id, EmployeeActualStagingUpdateRequest r) {
        EmployeeActualStaging value = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ACTUAL_STAGING_NOT_FOUND));
        Long reportId = value.getProductionReportStaging().getId();
        requireSameReport(reportId, r.getProductionReportStagingId());
        ProductionReportStaging report = editableReport(reportId);
        Long employeeId = r.getEmployeeId() == null ? value.getEmployee().getId() : r.getEmployeeId();
        Employee employee = activeEmployee(employeeId);
        AttendanceStatus attendance = r.getAttendanceStatus() == null ? value.getAttendanceStatus() : r.getAttendanceStatus();
        Integer working = r.getWorkingMinutes() == null ? value.getWorkingMinutes() : r.getWorkingMinutes();
        ensureUnique(reportId, employeeId, id);
        validateAttendance(attendance, working);
        value.setProductionReportStaging(report); value.setEmployee(employee);
        value.setAttendanceStatus(attendance); value.setWorkingMinutes(working);
        if (r.getOvertimeMinutes() != null) value.setOvertimeMinutes(r.getOvertimeMinutes());
        if (r.getAssignmentType() != null) value.setAssignmentType(r.getAssignmentType());
        if (r.getDescription() != null) value.setDescription(trimToNull(r.getDescription()));
        if (r.getActive() != null) value.setActive(r.getActive());
        return mapper.mapToResponse(save(value));
    }

    @Override @Transactional
    public void delete(Long id) {
        EmployeeActualStaging value = activeValue(id);
        editableReport(value.getProductionReportStaging().getId());
        value.setActive(false);
    }

    private ProductionReportStaging editableReport(Long id) {
        ProductionReportStaging report = reportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_REPORT_STAGING_NOT_FOUND));
        if (report.getStatus() != ProductionReportStatus.DRAFT)
            throw new AppException(ErrorCode.STAGING_DETAIL_REQUIRES_DRAFT);
        return report;
    }
    private void requireSameReport(Long currentId, Long requestedId) { if (requestedId != null && !requestedId.equals(currentId)) throw new AppException(ErrorCode.STAGING_DETAIL_PARENT_IMMUTABLE); }
    private Employee activeEmployee(Long id) { return employeeRepository.findByIdAndActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue(id).orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ID_NOT_FOUND)); }
    private EmployeeActualStaging activeValue(Long id) { return repository.findByIdAndActiveTrue(id).orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ACTUAL_STAGING_NOT_FOUND)); }
    private void validateAttendance(AttendanceStatus status, Integer working) { if (status == AttendanceStatus.ABSENT && working != 0) throw new AppException(ErrorCode.ABSENT_EMPLOYEE_HAS_WORKING_MINUTES); }
    private void ensureUnique(Long reportId, Long employeeId, Long id) { boolean exists = id == null ? repository.existsByProductionReportStaging_IdAndEmployee_Id(reportId, employeeId) : repository.existsByProductionReportStaging_IdAndEmployee_IdAndIdNot(reportId, employeeId, id); if (exists) throw new AppException(ErrorCode.EMPLOYEE_ACTUAL_STAGING_EXISTS); }
    private EmployeeActualStaging save(EmployeeActualStaging v) { try { return repository.saveAndFlush(v); } catch (DataIntegrityViolationException e) { throw new AppException(ErrorCode.EMPLOYEE_ACTUAL_STAGING_EXISTS); } }
    private List<EmployeeActualStagingResponse> map(List<EmployeeActualStaging> list) { return list.stream().map(mapper::mapToResponse).toList(); }
    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
