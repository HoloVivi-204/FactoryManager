package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.response.ReportImportBatchResponse;
import com.factory.management.dto.response.ReportImportRowErrorResponse;
import com.factory.management.entity.*;
import com.factory.management.entity.ProductionReportStatus;
import com.factory.management.exception.CloseWorkflowException;
import com.factory.management.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportImportPersistenceService {
    private final DailyCloseBatchRepository batchRepository;
    private final DailyCloseBatchItemRepository itemRepository;
    private final GeneratedReportFileRepository fileRepository;
    private final ReportImportBatchRepository importRepository;
    private final ReportImportRowErrorRepository errorRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ImportRegistration registerUpload(
            Long closeBatchId,
            String uploadedFileName,
            String contentType,
            byte[] content,
            String fileHash,
            String actor
    ) {
        var existing = importRepository.findByCloseBatch_IdAndFileHashSha256(closeBatchId, fileHash);
        if (existing.isPresent()) return new ImportRegistration(existing.get().getId(), false);

        DailyCloseBatch batch = batchRepository.findById(closeBatchId)
                .orElseThrow(() -> CloseWorkflowException.notFound(1401, "Lô chốt ngày không tồn tại"));
        if (batch.getStatus() != DailyCloseBatchStatus.FILE_GENERATED
                && batch.getStatus() != DailyCloseBatchStatus.IMPORT_FAILED) {
            throw CloseWorkflowException.conflict(1409,
                    "Lô chốt phải có file đã tạo và chưa khóa trước khi upload");
        }
        GeneratedReportFile generatedFile = fileRepository.findTopByCloseBatch_IdOrderByFileVersionDesc(closeBatchId)
                .orElseThrow(() -> CloseWorkflowException.conflict(1407, "Lô chốt chưa có file Excel được tạo"));
        LocalDateTime now = LocalDateTime.now();
        ReportImportBatch value = importRepository.save(ReportImportBatch.builder()
                .importNo("IMP-" + now.toLocalDate().toString().replace("-", "") + "-"
                        + UUID.randomUUID().toString().substring(0, 12).toUpperCase())
                .closeBatch(batch)
                .generatedFile(generatedFile)
                .status(ReportImportStatus.UPLOADED)
                .uploadedFileName(limit(uploadedFileName, 255))
                .contentType(limit(contentType, 100))
                .contentLength((long) content.length)
                .fileHashSha256(fileHash)
                .uploadedContent(content.clone())
                .totalRows(0)
                .successfulRows(0)
                .errorCount(0)
                .uploadedAt(now)
                .uploadedBy(actor)
                .build());
        return new ImportRegistration(value.getId(), true);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markValidationFailed(Long importId, ReportExcelService.ValidationResult validation) {
        ReportImportBatch value = find(importId);
        errorRepository.deleteAllByImportBatch_Id(importId);
        List<ReportImportRowError> errors = validation.errors().stream()
                .map(error -> ReportImportRowError.builder()
                        .importBatch(value)
                        .sheetName(limit(error.sheetName(), 100))
                        .rowNumber(error.rowNumber())
                        .columnName(limit(error.columnName(), 100))
                        .errorCode(limit(error.errorCode(), 80))
                        .message(limit(error.message(), 1000))
                        .rawValue(limit(error.rawValue(), 1000))
                        .createdAt(LocalDateTime.now())
                        .build())
                .toList();
        errorRepository.saveAll(errors);
        value.setStatus(ReportImportStatus.VALIDATION_FAILED);
        value.setTotalRows(validation.dataRowCount());
        value.setSuccessfulRows(0);
        value.setErrorCount(errors.size());
        value.setValidatedAt(LocalDateTime.now());
        value.setFailureMessage("File có " + errors.size() + " lỗi kiểm tra");
        markBatchImportFailed(value.getCloseBatch());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markValidated(Long importId, int totalRows) {
        ReportImportBatch value = find(importId);
        value.setStatus(ReportImportStatus.VALIDATED);
        value.setTotalRows(totalRows);
        value.setSuccessfulRows(0);
        value.setErrorCount(0);
        value.setFailureMessage(null);
        value.setValidatedAt(LocalDateTime.now());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markExecutionFailed(Long importId, String message) {
        ReportImportBatch value = find(importId);
        value.setStatus(ReportImportStatus.FAILED);
        value.setSuccessfulRows(0);
        value.setErrorCount(Math.max(value.getErrorCount(), 1));
        value.setFailureMessage(limit(message, 2000));
        markBatchImportFailed(value.getCloseBatch());
    }

    @Transactional(readOnly = true)
    public ReportImportBatchResponse get(Long importId) {
        return response(find(importId));
    }

    @Transactional(readOnly = true)
    public List<ReportImportBatchResponse> list(Long closeBatchId) {
        ensureBatch(closeBatchId);
        return importRepository.findAllByCloseBatch_IdOrderByUploadedAtDesc(closeBatchId).stream()
                .map(this::response)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ReportImportRowErrorResponse> errors(Long importId) {
        find(importId);
        return errorRepository.findAllByImportBatch_IdOrderBySheetNameAscRowNumberAscIdAsc(importId).stream()
                .map(value -> ReportImportRowErrorResponse.builder()
                        .id(value.getId())
                        .importBatchId(value.getImportBatch().getId())
                        .sheetName(value.getSheetName())
                        .rowNumber(value.getRowNumber())
                        .columnName(value.getColumnName())
                        .errorCode(value.getErrorCode())
                        .message(value.getMessage())
                        .rawValue(value.getRawValue())
                        .createdAt(value.getCreatedAt())
                        .build())
                .toList();
    }

    private void markBatchImportFailed(DailyCloseBatch batch) {
        batch.setStatus(DailyCloseBatchStatus.IMPORT_FAILED);
        itemRepository.findAllByCloseBatch_IdOrderBySequenceNo(batch.getId()).forEach(item -> {
            if (item.getOfficialReport() == null) item.getStagingReport().setStatus(ProductionReportStatus.IMPORT_FAILED);
        });
    }

    private ReportImportBatch find(Long id) {
        return importRepository.findById(id)
                .orElseThrow(() -> CloseWorkflowException.notFound(1410, "Lần import không tồn tại"));
    }

    private void ensureBatch(Long id) {
        if (!batchRepository.existsById(id)) {
            throw CloseWorkflowException.notFound(1401, "Lô chốt ngày không tồn tại");
        }
    }

    private ReportImportBatchResponse response(ReportImportBatch value) {
        return ReportImportBatchResponse.builder()
                .id(value.getId())
                .version(value.getVersion())
                .importNo(value.getImportNo())
                .closeBatchId(value.getCloseBatch().getId())
                .closeBatchNo(value.getCloseBatch().getBatchNo())
                .generatedFileId(value.getGeneratedFile().getId())
                .generatedFileVersion(value.getGeneratedFile().getFileVersion())
                .status(value.getStatus())
                .uploadedFileName(value.getUploadedFileName())
                .contentType(value.getContentType())
                .contentLength(value.getContentLength())
                .fileHashSha256(value.getFileHashSha256())
                .totalRows(value.getTotalRows())
                .successfulRows(value.getSuccessfulRows())
                .errorCount(value.getErrorCount())
                .failureMessage(value.getFailureMessage())
                .uploadedAt(value.getUploadedAt())
                .uploadedBy(value.getUploadedBy())
                .validatedAt(value.getValidatedAt())
                .importedAt(value.getImportedAt())
                .importedBy(value.getImportedBy())
                .build();
    }

    private String limit(String value, int max) {
        if (value == null) return null;
        return value.length() <= max ? value : value.substring(0, max);
    }

    public record ImportRegistration(Long importId, boolean created) {}
}
