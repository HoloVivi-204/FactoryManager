package com.factory.management.service.impl;

import com.factory.management.dto.request.MachineDowntimeStagingRequest;
import com.factory.management.dto.request.MachineDowntimeStagingUpdateRequest;
import com.factory.management.dto.response.MachineDowntimeStagingResponse;
import com.factory.management.entity.DowntimeReason;
import com.factory.management.entity.Machine;
import com.factory.management.entity.MachineDowntimeStaging;
import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.ProductionReportStatus;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.MachineDowntimeStagingMapper;
import com.factory.management.repository.DowntimeReasonRepository;
import com.factory.management.repository.MachineDowntimeStagingRepository;
import com.factory.management.repository.MachineRepository;
import com.factory.management.repository.ProductionReportStagingRepository;
import com.factory.management.service.MachineDowntimeStagingService;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachineDowntimeStagingServiceImpl implements MachineDowntimeStagingService {
    MachineDowntimeStagingRepository repository;
    ProductionReportStagingRepository reportRepository;
    MachineRepository machineRepository;
    DowntimeReasonRepository reasonRepository;
    MachineDowntimeStagingMapper mapper;

    @Override
    @Transactional
    public MachineDowntimeStagingResponse create(MachineDowntimeStagingRequest request) {
        ProductionReportStaging report = editableReport(request.getProductionReportStagingId());
        Machine machine = activeMachine(request.getMachineId());
        DowntimeReason reason = activeReason(request.getDowntimeReasonId());
        validateMachine(report, machine);

        MachineDowntimeStaging value = MachineDowntimeStaging.builder()
                .productionReportStaging(report)
                .machine(machine)
                .downtimeReason(reason)
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .durationMinutes(duration(request.getStartTime(), request.getEndTime()))
                .description(trimToNull(request.getDescription()))
                .active(request.getActive() == null ? true : request.getActive())
                .build();

        return mapper.mapToResponse(repository.save(value));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MachineDowntimeStagingResponse> getAll() {
        return map(repository.findAllByActiveTrue());
    }

    @Override
    @Transactional(readOnly = true)
    public List<MachineDowntimeStagingResponse> getByReportId(Long id) {
        return map(repository.findAllByProductionReportStaging_IdAndActiveTrue(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MachineDowntimeStagingResponse> getByMachineId(Long id) {
        return map(repository.findAllByMachine_IdAndActiveTrue(id));
    }

    @Override
    @Transactional(readOnly = true)
    public MachineDowntimeStagingResponse getById(Long id) {
        return mapper.mapToResponse(activeDowntime(id));
    }

    @Override
    @Transactional
    public MachineDowntimeStagingResponse update(Long id, MachineDowntimeStagingUpdateRequest request) {
        MachineDowntimeStaging value = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_DOWNTIME_STAGING_NOT_FOUND));
        Long reportId = value.getProductionReportStaging().getId();
        requireSameReport(reportId, request.getProductionReportStagingId());

        ProductionReportStaging report = editableReport(reportId);
        Long machineId = request.getMachineId() == null ? value.getMachine().getId() : request.getMachineId();
        Long reasonId = request.getDowntimeReasonId() == null
                ? value.getDowntimeReason().getId()
                : request.getDowntimeReasonId();
        LocalDateTime start = request.getStartTime() == null ? value.getStartTime() : request.getStartTime();
        LocalDateTime end = request.getEndTime() == null ? value.getEndTime() : request.getEndTime();
        Machine machine = activeMachine(machineId);
        DowntimeReason reason = activeReason(reasonId);
        validateMachine(report, machine);

        value.setProductionReportStaging(report);
        value.setMachine(machine);
        value.setDowntimeReason(reason);
        value.setStartTime(start);
        value.setEndTime(end);
        value.setDurationMinutes(duration(start, end));
        if (request.getDescription() != null) {
            value.setDescription(trimToNull(request.getDescription()));
        }
        if (request.getActive() != null) {
            value.setActive(request.getActive());
        }

        return mapper.mapToResponse(repository.save(value));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        MachineDowntimeStaging value = activeDowntime(id);
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

    private Machine activeMachine(Long id) {
        return machineRepository.findActiveByIdInActiveHierarchy(id)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_ID_NOT_FOUND));
    }

    private DowntimeReason activeReason(Long id) {
        return reasonRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOWNTIME_REASON_ID_NOT_FOUND));
    }

    private MachineDowntimeStaging activeDowntime(Long id) {
        return repository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_DOWNTIME_STAGING_NOT_FOUND));
    }

    private void validateMachine(ProductionReportStaging report, Machine machine) {
        if (!report.getMachine().getId().equals(machine.getId())) {
            throw new AppException(ErrorCode.MACHINE_DOWNTIME_MACHINE_MISMATCH);
        }
    }

    private int duration(LocalDateTime start, LocalDateTime end) {
        long minutes = ChronoUnit.MINUTES.between(start, end);
        if (!end.isAfter(start) || minutes < 1) {
            throw new AppException(ErrorCode.INVALID_DOWNTIME_TIME_RANGE);
        }

        return Math.toIntExact(minutes);
    }

    private List<MachineDowntimeStagingResponse> map(List<MachineDowntimeStaging> values) {
        return values.stream()
                .map(mapper::mapToResponse)
                .toList();
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
