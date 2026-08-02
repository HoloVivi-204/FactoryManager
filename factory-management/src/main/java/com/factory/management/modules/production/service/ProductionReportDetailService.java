package com.factory.management.modules.production.service;

import com.factory.management.modules.production.dto.response.ProductionReportDetailResponse;

public interface ProductionReportDetailService {
    ProductionReportDetailResponse getByReportId(Long reportId);
}
