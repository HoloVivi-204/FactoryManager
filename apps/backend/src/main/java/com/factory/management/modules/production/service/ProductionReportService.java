package com.factory.management.modules.production.service;

import com.factory.management.modules.production.dto.response.ProductionDashboardResponse;
import com.factory.management.modules.production.dto.response.ProductionReportResponse;
import com.factory.management.modules.masterdata.entity.Employee;
import com.factory.management.modules.production.entity.ProductionReportStaging;
import java.time.LocalDate;
import java.util.List;

public interface ProductionReportService {
    ProductionReportResponse createFromApprovedStaging(ProductionReportStaging staging, Employee approver, String remark);
    List<ProductionReportResponse> getAll();
    ProductionReportResponse getById(Long id);
    ProductionReportResponse getByReportNo(String reportNo);
    List<ProductionReportResponse> getByDate(LocalDate date);
    List<ProductionReportResponse> getByTeamId(Long teamId);
    List<ProductionReportResponse> getByMachineId(Long machineId);
    List<ProductionReportResponse> search(
            LocalDate fromDate,
            LocalDate toDate,
            Long factoryId,
            Long teamId,
            Long machineId
    );
    ProductionDashboardResponse getDashboard();
    ProductionDashboardResponse getMyDashboard();
}
