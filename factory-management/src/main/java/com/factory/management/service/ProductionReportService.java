package com.factory.management.service;

import com.factory.management.dto.response.ProductionDashboardResponse;
import com.factory.management.dto.response.ProductionReportResponse;
import com.factory.management.entity.Employee;
import com.factory.management.entity.ProductionReportStaging;
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
