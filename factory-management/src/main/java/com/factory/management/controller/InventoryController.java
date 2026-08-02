package com.factory.management.controller;

import com.factory.management.dto.request.InventoryReversalRequest;
import com.factory.management.dto.request.InventoryTransactionRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.InventoryStockResponse;
import com.factory.management.dto.response.InventoryTransactionResponse;
import com.factory.management.dto.response.PageResponse;
import com.factory.management.entity.InventoryTransactionType;
import com.factory.management.service.impl.InventoryService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/inventory")
@PreAuthorize("hasAnyRole('ADMIN','DIRECTOR','FINANCE','FACTORY_MANAGER')")
public class InventoryController {
    private final InventoryService service;

    @PostMapping("/transactions")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<InventoryTransactionResponse> create(
            @Valid @RequestBody InventoryTransactionRequest request
    ) {
        return ok(service.create(request));
    }

    @PostMapping("/transactions/{id}/reverse")
    @PreAuthorize("hasAnyRole('ADMIN','FINANCE')")
    public ApiResponse<InventoryTransactionResponse> reverse(
            @PathVariable Long id,
            @Valid @RequestBody InventoryReversalRequest request
    ) {
        return ApiResponse.<InventoryTransactionResponse>builder()
                .message("Đã tạo giao dịch đảo kho; giao dịch gốc được giữ để đối soát")
                .result(service.reverse(id, request.getReason()))
                .build();
    }

    @GetMapping("/transactions")
    public ApiResponse<List<InventoryTransactionResponse>> search(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) InventoryTransactionType type
    ) {
        return ok(service.search(fromDate, toDate, warehouseId, materialId, type));
    }

    @GetMapping("/transactions/page")
    public ApiResponse<PageResponse<InventoryTransactionResponse>> searchPage(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) Long materialId,
            @RequestParam(required = false) InventoryTransactionType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size
    ) {
        return ok(service.searchPage(fromDate, toDate, warehouseId, materialId, type, page, size));
    }

    @GetMapping("/stocks")
    public ApiResponse<List<InventoryStockResponse>> stocks(@RequestParam(required = false) Long warehouseId) {
        return ok(service.stocks(warehouseId));
    }

    private <T> ApiResponse<T> ok(T value) {
        return ApiResponse.<T>builder().result(value).build();
    }
}
