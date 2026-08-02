package com.factory.management.controller;

import com.factory.management.dto.request.FinancialPaymentRequest;
import com.factory.management.dto.request.FinancialPaymentReversalRequest;
import com.factory.management.dto.request.FinancialRecordRequest;
import com.factory.management.dto.request.FinancialVoidRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.FinancialPaymentResponse;
import com.factory.management.dto.response.FinancialRecordResponse;
import com.factory.management.dto.response.FinancialSummaryResponse;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.entity.FinancialEntryType;
import com.factory.management.entity.FinancialRecordStatus;
import com.factory.management.service.ServiceImpl.FinancialRecordService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/financial-records")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FINANCE','FACTORY_MANAGER')")
public class FinancialRecordController {
    private final FinancialRecordService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<FinancialRecordResponse> create(@Valid @RequestBody FinancialRecordRequest request) {
        return one(service.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<FinancialRecordResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody FinancialRecordRequest request
    ) {
        return one(service.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<Void> deleteDraft(@PathVariable Long id) {
        service.delete(id);
        return ApiResponse.<Void>builder().message("Đã ngừng bản ghi tài chính nháp").build();
    }

    @PostMapping("/{id}/post")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<FinancialRecordResponse> post(@PathVariable Long id) {
        return ApiResponse.<FinancialRecordResponse>builder()
                .message("Đã ghi sổ bản ghi tài chính")
                .result(service.post(id))
                .build();
    }

    @PostMapping("/{id}/void")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<FinancialRecordResponse> voidRecord(
            @PathVariable Long id,
            @Valid @RequestBody FinancialVoidRequest request
    ) {
        return ApiResponse.<FinancialRecordResponse>builder()
                .message("Đã hủy ghi sổ; lịch sử chứng từ vẫn được giữ")
                .result(service.voidRecord(id, request.getReason()))
                .build();
    }

    @PostMapping("/{id}/payments")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<FinancialPaymentResponse> addPayment(
            @PathVariable Long id,
            @Valid @RequestBody FinancialPaymentRequest request
    ) {
        return ApiResponse.<FinancialPaymentResponse>builder()
                .message("Đã ghi nhận thanh toán")
                .result(service.addPayment(id, request))
                .build();
    }

    @GetMapping("/{id}/payments")
    public ApiResponse<List<FinancialPaymentResponse>> payments(@PathVariable Long id) {
        return ApiResponse.<List<FinancialPaymentResponse>>builder()
                .result(service.payments(id))
                .build();
    }

    @PostMapping("/{recordId}/payments/{paymentId}/reverse")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<FinancialPaymentResponse> reversePayment(
            @PathVariable Long recordId,
            @PathVariable Long paymentId,
            @Valid @RequestBody FinancialPaymentReversalRequest request
    ) {
        return ApiResponse.<FinancialPaymentResponse>builder()
                .message("Đã đảo thanh toán; lịch sử gốc được giữ để đối soát")
                .result(service.reversePayment(recordId, paymentId, request.getReason()))
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<FinancialRecordResponse>> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long factoryId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long productionLineId,
            @RequestParam(required = false) FinancialEntryType type,
            @RequestParam(required = false) FinancialRecordStatus status
    ) {
        return ApiResponse.<List<FinancialRecordResponse>>builder()
                .result(service.search(fromDate, toDate, factoryId, departmentId,
                        productionLineId, type, status))
                .build();
    }

    @GetMapping("/search/page")
    public ApiResponse<PageResponse<FinancialRecordResponse>> searchPage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long factoryId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long productionLineId,
            @RequestParam(required = false) FinancialEntryType type,
            @RequestParam(required = false) FinancialRecordStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ApiResponse.<PageResponse<FinancialRecordResponse>>builder()
                .result(service.searchPage(fromDate, toDate, factoryId, departmentId,
                        productionLineId, type, status, page, size))
                .build();
    }

    @GetMapping("/dashboard")
    public ApiResponse<FinancialSummaryResponse> dashboard(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long factoryId,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long productionLineId
    ) {
        return ApiResponse.<FinancialSummaryResponse>builder()
                .result(service.summary(fromDate, toDate, factoryId, departmentId, productionLineId))
                .build();
    }

    private ApiResponse<FinancialRecordResponse> one(FinancialRecordResponse value) {
        return ApiResponse.<FinancialRecordResponse>builder().result(value).build();
    }
}
