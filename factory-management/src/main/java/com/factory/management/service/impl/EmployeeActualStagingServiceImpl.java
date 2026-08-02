package com.factory.management.service.impl;

import com.factory.management.dto.request.EmployeeActualStagingRequest;
import com.factory.management.dto.request.EmployeeActualStagingUpdateRequest;
import com.factory.management.dto.response.EmployeeActualStagingResponse;
import com.factory.management.entity.AttendanceStatus;
import com.factory.management.entity.Employee;
import com.factory.management.entity.EmployeeActualStaging;
import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.ProductionReportStatus;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.EmployeeActualStagingMapper;
import com.factory.management.repository.EmployeeActualStagingRepository;
import com.factory.management.repository.EmployeeRepository;
import com.factory.management.repository.ProductionReportStagingRepository;
import com.factory.management.service.EmployeeActualStagingService;
import java.util.List;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeActualStagingServiceImpl implements EmployeeActualStagingService {
    EmployeeActualStagingRepository repository;
    ProductionReportStagingRepository reportRepository;
    EmployeeRepository employeeRepository;
    EmployeeActualStagingMapper mapper;

    @Override
    @Transactional
    public EmployeeActualStagingResponse create(EmployeeActualStagingRequest request) {
        ProductionReportStaging report = editableReport(request.getProductionReportStagingId());
        Employee employee = activeEmployee(request.getEmployeeId());
        ensureUnique(report.getId(), employee.getId(), null);
        validateAttendance(request.getAttendanceStatus(), request.getWorkingMinutes());

        EmployeeActualStaging value = EmployeeActualStaging.builder()
                .productionReportStaging(report)
                .employee(employee)
                .workingMinutes(request.getWorkingMinutes())
                .overtimeMinutes(request.getOvertimeMinutes())
                .attendanceStatus(request.getAttendanceStatus())
                .assignmentType(request.getAssignmentType())
                .description(trimToNull(request.getDescription()))
                .active(request.getActive() == null ? true : request.getActive())
                .build();

        return mapper.mapToResponse(save(value));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeActualStagingResponse> getAll() {
        return map(repository.findAllByActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeActualStagingResponse> getByReportId(Long id) {
        return map(repository.findAllByProductionReportStaging_IdAndActiveTrue(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeActualStagingResponse> getByEmployeeId(Long id) {
        return map(repository.findAllByEmployee_IdAndActiveTrue(id));
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeActualStagingResponse getById(Long id) {
        return mapper.mapToResponse(activeValue(id));
    }

    @Override
    @Transactional
    public EmployeeActualStagingResponse update(Long id, EmployeeActualStagingUpdateRequest request) {
        EmployeeActualStaging value = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ACTUAL_STAGING_NOT_FOUND));
        Long reportId = value.getProductionReportStaging().getId();
        requireSameReport(reportId, request.getProductionReportStagingId());

        ProductionReportStaging report = editableReport(reportId);
        Long employeeId = request.getEmployeeId() == null ? value.getEmployee().getId() : request.getEmployeeId();
        Employee employee = activeEmployee(employeeId);
        AttendanceStatus attendance = request.getAttendanceStatus() == null
                ? value.getAttendanceStatus()
                : request.getAttendanceStatus();
        Integer workingMinutes = request.getWorkingMinutes() == null
                ? value.getWorkingMinutes()
                : request.getWorkingMinutes();

        ensureUnique(reportId, employeeId, id);
        validateAttendance(attendance, workingMinutes);

        value.setProductionReportStaging(report);
        value.setEmployee(employee);
        value.setAttendanceStatus(attendance);
        value.setWorkingMinutes(workingMinutes);
        if (request.getOvertimeMinutes() != null) {
            value.setOvertimeMinutes(request.getOvertimeMinutes());
        }
        if (request.getAssignmentType() != null) {
            value.setAssignmentType(request.getAssignmentType());
        }
        if (request.getDescription() != null) {
            value.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getActive() != null) {
            value.setActive(request.getActive());
        }

        return mapper.mapToResponse(save(value));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        EmployeeActualStaging value = activeValue(id);
        editableReport(value.getProductionReportStaging().getId());
        value.setActive(false);
    }

    private ProductionReportStaging editableReport(Long id) {
        ProductionReportStaging report = reportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_REPORT_STAGING_NOT_FOUND));
        if (report.getStatus() != ProductionReportStatus.DRAFT) {
            throw new AppException(ErrorCode.STAGING_DETAIL_REQUIRES_DRAFT);
        }

        return report;
    }

    private void requireSameReport(Long currentId, Long requestedId) {
        if (requestedId != null && !requestedId.equals(currentId)) {
            throw new AppException(ErrorCode.STAGING_DETAIL_PARENT_IMMUTABLE);
        }
    }

    private Employee activeEmployee(Long id) {
        return employeeRepository.findActiveByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ID_NOT_FOUND));
    }

    private EmployeeActualStaging activeValue(Long id) {
        return repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ACTUAL_STAGING_NOT_FOUND));
    }

    private void validateAttendance(AttendanceStatus status, Integer workingMinutes) {
        if (status == AttendanceStatus.ABSENT && workingMinutes != 0) {
            throw new AppException(ErrorCode.ABSENT_EMPLOYEE_HAS_WORKING_MINUTES);
        }
    }

    private void ensureUnique(Long reportId, Long employeeId, Long id) {
        boolean exists = id == null
                ? repository.existsByProductionReportStaging_IdAndEmployee_Id(reportId, employeeId)
                : repository.existsByProductionReportStaging_IdAndEmployee_IdAndIdNot(reportId, employeeId, id);
        if (exists) {
            throw new AppException(ErrorCode.EMPLOYEE_ACTUAL_STAGING_EXISTS);
        }
    }

    private EmployeeActualStaging save(EmployeeActualStaging value) {
        try {
            return repository.saveAndFlush(value);
        } catch (DataIntegrityViolationException e) {
            throw new AppException(ErrorCode.EMPLOYEE_ACTUAL_STAGING_EXISTS);
        }
    }

    private List<EmployeeActualStagingResponse> map(List<EmployeeActualStaging> values) {
        return values.stream()
                .map(mapper::mapToResponse)
                .toList();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
