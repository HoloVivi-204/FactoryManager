package com.factory.management.controller;

import com.factory.management.dto.request.DowntimeReasonRequest;
import com.factory.management.dto.request.DowntimeReasonUpdateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.DowntimeReasonResponse;
import com.factory.management.entity.DowntimeReasonType;
import com.factory.management.service.Service.DowntimeReasonService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/downtime-reasons")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DowntimeReasonController {
    DowntimeReasonService downtimeReasonService;

    @PostMapping
    ApiResponse<DowntimeReasonResponse> create(@Valid @RequestBody DowntimeReasonRequest request) {
        return ApiResponse.<DowntimeReasonResponse>builder()
                .result(downtimeReasonService.create(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<DowntimeReasonResponse>> getAll() {
        return ApiResponse.<List<DowntimeReasonResponse>>builder()
                .result(downtimeReasonService.getAll())
                .build();
    }

    @GetMapping("/type/{reasonType}")
    ApiResponse<List<DowntimeReasonResponse>> getAllByReasonType(
            @PathVariable DowntimeReasonType reasonType
    ) {
        return ApiResponse.<List<DowntimeReasonResponse>>builder()
                .result(downtimeReasonService.getAllByReasonType(reasonType))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<DowntimeReasonResponse> getById(@PathVariable Long id) {
        return ApiResponse.<DowntimeReasonResponse>builder()
                .result(downtimeReasonService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<DowntimeReasonResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody DowntimeReasonUpdateRequest request
    ) {
        return ApiResponse.<DowntimeReasonResponse>builder()
                .result(downtimeReasonService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        downtimeReasonService.delete(id);
        return ApiResponse.<String>builder()
                .message("Xóa lý do dừng máy thành công")
                .build();
    }
}
