package com.factory.management.service;

import com.factory.management.dto.response.ProductionReportDetailResponse;

public interface ProductionReportDetailService {
    ProductionReportDetailResponse getByReportId(Long reportId);
}
