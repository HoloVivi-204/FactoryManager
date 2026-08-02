package com.factory.management.modules.production.service;

import com.factory.management.modules.production.dto.response.ProductionReportDetailResponse.DowntimeItem;
import com.factory.management.modules.production.dto.response.ProductionReportDetailResponse.EmployeeItem;
import com.factory.management.modules.production.dto.response.ProductionReportDetailResponse.MaterialItem;
import com.factory.management.modules.production.dto.response.ProductionReportDetailResponse.QualityItem;
import com.factory.management.modules.production.dto.response.ProductionReportDetailResponse;
import com.factory.management.modules.production.entity.EmployeeActual;
import com.factory.management.modules.production.entity.MachineDowntime;
import com.factory.management.modules.production.entity.MaterialIssue;
import com.factory.management.modules.production.entity.ProductionReport;
import com.factory.management.modules.production.entity.QualityReport;
import com.factory.management.common.error.AppException;
import com.factory.management.common.error.ErrorCode;
import com.factory.management.modules.production.mapper.ProductionReportMapper;
import com.factory.management.modules.production.repository.EmployeeActualRepository;
import com.factory.management.modules.production.repository.MachineDowntimeRepository;
import com.factory.management.modules.production.repository.MaterialIssueRepository;
import com.factory.management.modules.production.repository.ProductionReportRepository;
import com.factory.management.modules.production.repository.QualityReportRepository;
import com.factory.management.modules.production.service.ProductionReportDetailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductionReportDetailServiceImpl implements ProductionReportDetailService {
    private final ProductionReportRepository reportRepository;
    private final MachineDowntimeRepository downtimeRepository;
    private final QualityReportRepository qualityRepository;
    private final MaterialIssueRepository materialRepository;
    private final EmployeeActualRepository employeeRepository;
    private final ProductionReportMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public ProductionReportDetailResponse getByReportId(Long id) {
        ProductionReport report = reportRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCTION_REPORT_NOT_FOUND));

        return ProductionReportDetailResponse.builder()
                .report(mapper.mapToResponse(report))
                .downtimes(downtimeRepository.findAllByProductionReport_Id(id).stream()
                        .map(this::downtime)
                        .toList())
                .qualityErrors(qualityRepository.findAllByProductionReport_Id(id).stream()
                        .map(this::quality)
                        .toList())
                .materialIssues(materialRepository.findAllByProductionReport_Id(id).stream()
                        .map(this::material)
                        .toList())
                .employees(employeeRepository.findAllByProductionReport_Id(id).stream()
                        .map(this::employee)
                        .toList())
                .build();
    }

    private DowntimeItem downtime(MachineDowntime value) {
        return DowntimeItem.builder()
                .id(value.getId())
                .machineId(value.getMachine().getId())
                .machineCode(value.getMachine().getCode())
                .downtimeReasonId(value.getDowntimeReason().getId())
                .downtimeReasonName(value.getDowntimeReason().getName())
                .startTime(value.getStartTime())
                .endTime(value.getEndTime())
                .durationMinutes(value.getDurationMinutes())
                .description(value.getDescription())
                .build();
    }

    private QualityItem quality(QualityReport value) {
        return QualityItem.builder()
                .id(value.getId())
                .qualityErrorTypeId(value.getQualityErrorType().getId())
                .qualityErrorTypeName(value.getQualityErrorType().getName())
                .quantity(value.getQuantity())
                .description(value.getDescription())
                .build();
    }

    private MaterialItem material(MaterialIssue value) {
        return MaterialItem.builder()
                .id(value.getId())
                .materialId(value.getMaterial().getId())
                .materialName(value.getMaterial().getName())
                .issueType(value.getIssueType())
                .quantity(value.getQuantity())
                .unit(value.getUnit())
                .description(value.getDescription())
                .build();
    }

    private EmployeeItem employee(EmployeeActual value) {
        return EmployeeItem.builder()
                .id(value.getId())
                .employeeId(value.getEmployee().getId())
                .employeeCode(value.getEmployee().getCode())
                .employeeName(value.getEmployee().getFullName())
                .workingMinutes(value.getWorkingMinutes())
                .overtimeMinutes(value.getOvertimeMinutes())
                .attendanceStatus(value.getAttendanceStatus())
                .assignmentType(value.getAssignmentType())
                .description(value.getDescription())
                .build();
    }
}
