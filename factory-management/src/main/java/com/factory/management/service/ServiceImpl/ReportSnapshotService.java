package com.factory.management.service.ServiceImpl;

import com.factory.management.entity.EmployeeActualStaging;
import com.factory.management.entity.MachineDowntimeStaging;
import com.factory.management.entity.MaterialIssueStaging;
import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.QualityReportStaging;
import com.factory.management.repository.EmployeeActualStagingRepository;
import com.factory.management.repository.MachineDowntimeStagingRepository;
import com.factory.management.repository.MaterialIssueStagingRepository;
import com.factory.management.repository.QualityReportStagingRepository;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportSnapshotService {
    private final MachineDowntimeStagingRepository downtimeRepository;
    private final QualityReportStagingRepository qualityRepository;
    private final MaterialIssueStagingRepository materialRepository;
    private final EmployeeActualStagingRepository employeeRepository;

    @Transactional(readOnly = true)
    public String hash(ProductionReportStaging report) {
        StringBuilder value = new StringBuilder();
        append(value, report.getId(), report.getReportDate(), report.getShift().getId(),
                report.getFactory().getId(), report.getDepartment().getId(), report.getProductionLine().getId(),
                report.getTeam().getId(), report.getLeaderEmployee().getId(), report.getMachine().getId(),
                report.getPlannedQuantity(), report.getActualQuantity(), report.getGoodQuantity(),
                report.getDefectQuantity(), report.getWorkingMinutes(), report.getDowntimeMinutes(), report.getNote());

        downtimeRepository.findAllByProductionReportStaging_IdAndActiveTrue(report.getId()).stream()
                .sorted(Comparator.comparing(MachineDowntimeStaging::getId))
                .forEach(item -> append(value, "D", item.getId(), item.getMachine().getId(),
                        item.getDowntimeReason().getId(), item.getStartTime(), item.getEndTime(),
                        item.getDurationMinutes(), item.getDescription()));
        qualityRepository.findAllByProductionReportStaging_IdAndActiveTrue(report.getId()).stream()
                .sorted(Comparator.comparing(QualityReportStaging::getId))
                .forEach(item -> append(value, "Q", item.getId(), item.getQualityErrorType().getId(),
                        item.getQuantity(), item.getDescription()));
        materialRepository.findAllByProductionReportStaging_IdAndActiveTrue(report.getId()).stream()
                .sorted(Comparator.comparing(MaterialIssueStaging::getId))
                .forEach(item -> append(value, "M", item.getId(), item.getMaterial().getId(),
                        item.getIssueType(), item.getQuantity(), item.getUnit(), item.getDescription()));
        employeeRepository.findAllByProductionReportStaging_IdAndActiveTrue(report.getId()).stream()
                .sorted(Comparator.comparing(EmployeeActualStaging::getId))
                .forEach(item -> append(value, "E", item.getId(), item.getEmployee().getId(),
                        item.getWorkingMinutes(), item.getOvertimeMinutes(), item.getAttendanceStatus(),
                        item.getAssignmentType(), item.getDescription()));
        return sha256(value.toString().getBytes(StandardCharsets.UTF_8));
    }

    public String sha256(byte[] content) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(content);
            StringBuilder result = new StringBuilder(digest.length * 2);
            for (byte value : digest) result.append(String.format("%02x", value));
            return result.toString();
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private void append(StringBuilder target, Object... values) {
        for (Object value : values) {
            String text = value == null ? "<null>" : String.valueOf(value);
            target.append(text.length()).append(':').append(text).append('|');
        }
        target.append('\n');
    }
}
