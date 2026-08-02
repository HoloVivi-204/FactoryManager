package com.factory.management.modules.production.controller;

import com.factory.management.common.api.ApiResponse;
import com.factory.management.modules.production.dto.response.StagingExcelImportResponse;
import com.factory.management.modules.production.service.StagingReportExcelService;
import java.nio.charset.StandardCharsets;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/staging-report-excel")
@PreAuthorize("hasAnyRole('ADMIN','FACTORY_MANAGER','DEPARTMENT_MANAGER','PRODUCTION_MANAGER','TEAM_LEADER')")
public class StagingReportExcelController {

    private final StagingReportExcelService service;

    @GetMapping("/template")
    public ResponseEntity<byte[]> template() {
        var file = service.template();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(file.fileName(), StandardCharsets.UTF_8).build().toString())
                .body(file.content());
    }

    @PostMapping(value = "/preview", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<StagingExcelImportResponse> preview(@RequestPart("file") MultipartFile file) {
        return ApiResponse.<StagingExcelImportResponse>builder()
                .message("Đã kiểm tra file Excel")
                .result(service.preview(file)).build();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<StagingExcelImportResponse> importWorkbook(@RequestPart("file") MultipartFile file) {
        StagingExcelImportResponse result = service.importWorkbook(file);
        return ApiResponse.<StagingExcelImportResponse>builder()
                .message(result.isImported() ? "Nhập dữ liệu Excel thành công" : "File còn lỗi nên chưa có dữ liệu nào được lưu")
                .result(result).build();
    }
}
