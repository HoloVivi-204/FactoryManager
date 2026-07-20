package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.*;
import com.factory.management.dto.response.MachineDowntimeStagingResponse;
import com.factory.management.entity.*;
import com.factory.management.exception.*;
import com.factory.management.mapper.MachineDowntimeStagingMapper;
import com.factory.management.repository.*;
import com.factory.management.service.Service.MachineDowntimeStagingService;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class MachineDowntimeStagingServiceImpl implements MachineDowntimeStagingService {
    MachineDowntimeStagingRepository repository;
    ProductionReportStagingRepository reportRepository;
    MachineRepository machineRepository;
    DowntimeReasonRepository reasonRepository;
    MachineDowntimeStagingMapper mapper;

    @Override @Transactional
    public MachineDowntimeStagingResponse create(MachineDowntimeStagingRequest r) {
        ProductionReportStaging report = editableReport(r.getProductionReportStagingId());
        Machine machine = activeMachine(r.getMachineId());
        DowntimeReason reason = activeReason(r.getDowntimeReasonId());
        validateMachine(report, machine);
        int duration = duration(r.getStartTime(), r.getEndTime());
        MachineDowntimeStaging value = MachineDowntimeStaging.builder()
                .productionReportStaging(report).machine(machine).downtimeReason(reason)
                .startTime(r.getStartTime()).endTime(r.getEndTime()).durationMinutes(duration)
                .description(trimToNull(r.getDescription()))
                .active(r.getActive() == null ? true : r.getActive()).build();
        return mapper.mapToResponse(repository.save(value));
    }

    @Override @Transactional(readOnly = true)
    public List<MachineDowntimeStagingResponse> getAll() { return map(repository.findAllByActiveTrue()); }
    @Override @Transactional(readOnly = true)
    public List<MachineDowntimeStagingResponse> getByReportId(Long id) { return map(repository.findAllByProductionReportStaging_IdAndActiveTrue(id)); }
    @Override @Transactional(readOnly = true)
    public List<MachineDowntimeStagingResponse> getByMachineId(Long id) { return map(repository.findAllByMachine_IdAndActiveTrue(id)); }
    @Override @Transactional(readOnly = true)
    public MachineDowntimeStagingResponse getById(Long id) { return mapper.mapToResponse(activeDowntime(id)); }

    @Override @Transactional
    public MachineDowntimeStagingResponse update(Long id, MachineDowntimeStagingUpdateRequest r) {
        MachineDowntimeStaging value = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MACHINE_DOWNTIME_STAGING_NOT_FOUND));
        Long reportId = value.getProductionReportStaging().getId();
        requireSameReport(reportId, r.getProductionReportStagingId());
        ProductionReportStaging report = editableReport(reportId);
        Long machineId = r.getMachineId() == null ? value.getMachine().getId() : r.getMachineId();
        Long reasonId = r.getDowntimeReasonId() == null ? value.getDowntimeReason().getId() : r.getDowntimeReasonId();
        LocalDateTime start = r.getStartTime() == null ? value.getStartTime() : r.getStartTime();
        LocalDateTime end = r.getEndTime() == null ? value.getEndTime() : r.getEndTime();
        Machine machine = activeMachine(machineId);
        DowntimeReason reason = activeReason(reasonId);
        validateMachine(report, machine);
        value.setProductionReportStaging(report); value.setMachine(machine); value.setDowntimeReason(reason);
        value.setStartTime(start); value.setEndTime(end); value.setDurationMinutes(duration(start, end));
        if (r.getDescription() != null) value.setDescription(trimToNull(r.getDescription()));
        if (r.getActive() != null) value.setActive(r.getActive());
        return mapper.mapToResponse(repository.save(value));
    }

    @Override @Transactional
    public void delete(Long id) {
        MachineDowntimeStaging value = activeDowntime(id);
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
    private Machine activeMachine(Long id) { return machineRepository.findByIdAndActiveTrueAndMachineType_ActiveTrueAndTeam_ActiveTrueAndTeam_ProductionLine_ActiveTrueAndTeam_ProductionLine_Department_ActiveTrueAndTeam_ProductionLine_Department_Factory_ActiveTrue(id).orElseThrow(() -> new AppException(ErrorCode.MACHINE_ID_NOT_FOUND)); }
    private DowntimeReason activeReason(Long id) { return reasonRepository.findByIdAndActiveTrue(id).orElseThrow(() -> new AppException(ErrorCode.DOWNTIME_REASON_ID_NOT_FOUND)); }
    private MachineDowntimeStaging activeDowntime(Long id) { return repository.findByIdAndActiveTrue(id).orElseThrow(() -> new AppException(ErrorCode.MACHINE_DOWNTIME_STAGING_NOT_FOUND)); }
    private void validateMachine(ProductionReportStaging report, Machine machine) { if (!report.getMachine().getId().equals(machine.getId())) throw new AppException(ErrorCode.MACHINE_DOWNTIME_MACHINE_MISMATCH); }
    private int duration(LocalDateTime start, LocalDateTime end) { long minutes = ChronoUnit.MINUTES.between(start, end); if (!end.isAfter(start) || minutes < 1) throw new AppException(ErrorCode.INVALID_DOWNTIME_TIME_RANGE); return Math.toIntExact(minutes); }
    private List<MachineDowntimeStagingResponse> map(List<MachineDowntimeStaging> list) { return list.stream().map(mapper::mapToResponse).toList(); }
    private String trimToNull(String value) { return value == null || value.isBlank() ? null : value.trim(); }
}
