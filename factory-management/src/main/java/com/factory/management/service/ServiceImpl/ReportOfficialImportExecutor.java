package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.response.ProductionReportResponse;
import com.factory.management.entity.DailyCloseBatch;
import com.factory.management.entity.DailyCloseBatchItem;
import com.factory.management.entity.DailyCloseBatchStatus;
import com.factory.management.entity.Employee;
import com.factory.management.entity.ProductionReport;
import com.factory.management.entity.ProductionReportStaging;
import com.factory.management.entity.ProductionReportStatus;
import com.factory.management.entity.ReportImportBatch;
import com.factory.management.entity.ReportImportStatus;
import com.factory.management.exception.CloseWorkflowException;
import com.factory.management.repository.DailyCloseBatchItemRepository;
import com.factory.management.repository.ProductionReportRepository;
import com.factory.management.repository.ReportImportBatchRepository;
import com.factory.management.service.Service.CurrentUserService;
import com.factory.management.service.Service.ProductionReportService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReportOfficialImportExecutor {
    private final ReportImportBatchRepository importRepository;
    private final DailyCloseBatchItemRepository itemRepository;
    private final ProductionReportRepository productionReportRepository;
    private final ProductionReportService productionReportService;
    private final ReportSnapshotService snapshotService;
    private final CurrentUserService currentUserService;
    private final EntityManager entityManager;

    @Transactional
    public int execute(Long importId) {
        ReportImportBatch importBatch = importRepository.findById(importId)
                .orElseThrow(() -> CloseWorkflowException.notFound(1410, "Lần import không tồn tại"));
        entityManager.lock(importBatch, LockModeType.PESSIMISTIC_WRITE);
        if (importBatch.getStatus() != ReportImportStatus.VALIDATED) {
            throw CloseWorkflowException.conflict(1411, "File chưa vượt qua bước kiểm tra hoặc đã được import");
        }

        DailyCloseBatch closeBatch = importBatch.getCloseBatch();
        entityManager.lock(closeBatch, LockModeType.PESSIMISTIC_WRITE);
        if (closeBatch.getStatus() != DailyCloseBatchStatus.FILE_GENERATED
                && closeBatch.getStatus() != DailyCloseBatchStatus.IMPORT_FAILED) {
            throw CloseWorkflowException.conflict(1409, "Trạng thái lô chốt không cho phép import");
        }

        List<DailyCloseBatchItem> items = itemRepository.findAllByCloseBatch_IdOrderBySequenceNo(closeBatch.getId());
        if (items.isEmpty()) {
            throw CloseWorkflowException.conflict(1404, "Lô chốt không có báo cáo để import");
        }

        importBatch.setStatus(ReportImportStatus.IMPORTING);
        Employee importer = currentUserService.employee();
        String actor = SecurityContextHolder.getContext().getAuthentication().getName();
        for (DailyCloseBatchItem item : items) {
            ProductionReportStaging staging = item.getStagingReport();
            entityManager.lock(staging, LockModeType.PESSIMISTIC_WRITE);
            if (item.getOfficialReport() != null || productionReportRepository.existsBySourceStaging_Id(staging.getId())) {
                throw CloseWorkflowException.conflict(1412,
                        "Báo cáo staging " + staging.getId() + " đã có dữ liệu chính thức");
            }
            if (staging.getStatus() != ProductionReportStatus.FILE_GENERATED
                    && staging.getStatus() != ProductionReportStatus.IMPORT_FAILED) {
                throw CloseWorkflowException.conflict(1413,
                        "Báo cáo staging " + staging.getId() + " không ở trạng thái sẵn sàng import");
            }
            String currentHash = snapshotService.hash(staging);
            if (!item.getSourceHash().equals(currentHash)) {
                throw CloseWorkflowException.conflict(1414,
                        "Dữ liệu staging " + staging.getId() + " đã thay đổi sau khi chốt");
            }

            ProductionReportResponse official = productionReportService.createFromApprovedStaging(
                    staging,
                    importer,
                    "Imported from close batch " + closeBatch.getBatchNo()
            );
            ProductionReport officialEntity = productionReportRepository.findById(official.getId())
                    .orElseThrow(() -> CloseWorkflowException.conflict(1415,
                            "Không tìm thấy báo cáo chính thức vừa tạo"));
            item.setOfficialReport(officialEntity);
            staging.setStatus(ProductionReportStatus.IMPORTED);
        }

        LocalDateTime now = LocalDateTime.now();
        importBatch.setStatus(ReportImportStatus.IMPORTED);
        importBatch.setSuccessfulRows(items.size());
        importBatch.setErrorCount(0);
        importBatch.setFailureMessage(null);
        importBatch.setImportedAt(now);
        importBatch.setImportedBy(actor);

        closeBatch.setStatus(DailyCloseBatchStatus.IMPORTED);
        closeBatch.setImportedAt(now);
        closeBatch.setImportedBy(actor);
        for (DailyCloseBatchItem item : items) item.getStagingReport().setStatus(ProductionReportStatus.LOCKED);
        closeBatch.setStatus(DailyCloseBatchStatus.LOCKED);
        closeBatch.setLockedAt(now);
        closeBatch.setLockedBy(actor);
        return items.size();
    }
}
