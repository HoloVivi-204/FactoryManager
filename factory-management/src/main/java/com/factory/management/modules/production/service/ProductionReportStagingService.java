package com.factory.management.modules.production.service;

import com.factory.management.modules.production.dto.request.ProductionReportApprovalRequest;
import com.factory.management.modules.production.dto.request.ProductionReportReviewRequest;
import com.factory.management.modules.production.dto.request.ProductionReportStagingRequest;
import com.factory.management.modules.production.dto.request.ProductionReportStagingUpdateRequest;
import com.factory.management.modules.production.dto.response.ProductionReportResponse;
import com.factory.management.modules.production.dto.response.ProductionReportStagingResponse;
import com.factory.management.modules.production.entity.ProductionReportStatus;
import java.time.LocalDate;
import java.util.List;

public interface ProductionReportStagingService {
    ProductionReportStagingResponse create(ProductionReportStagingRequest request);
    List<ProductionReportStagingResponse> getAll();
    List<ProductionReportStagingResponse> getMyScope();
    List<ProductionReportStagingResponse> getByDate(LocalDate date);
    List<ProductionReportStagingResponse> getByTeamId(Long teamId);
    List<ProductionReportStagingResponse> getByMachineId(Long machineId);
    List<ProductionReportStagingResponse> getByStatus(ProductionReportStatus status);
    ProductionReportStagingResponse getById(Long id);
    ProductionReportStagingResponse update(Long id, ProductionReportStagingUpdateRequest request);
    ProductionReportStagingResponse submit(Long id);
    ProductionReportStagingResponse requestChange(Long id, ProductionReportReviewRequest request);
    ProductionReportStagingResponse returnToDraft(Long id);
    ProductionReportResponse approve(Long id, ProductionReportApprovalRequest request);
    ProductionReportStagingResponse lock(Long id);
    void deleteDraft(Long id);
}
