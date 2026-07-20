package com.factory.management.controller;

import com.factory.management.dto.request.ShiftRequest;
import com.factory.management.dto.request.ShiftUpdateRequest;
import com.factory.management.dto.response.ApiResponse;
import com.factory.management.dto.response.ShiftResponse;
import com.factory.management.service.Service.ShiftService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("${api.prefix}/shifts")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ShiftController {
    ShiftService shiftService;

    @PostMapping
    ApiResponse<ShiftResponse> create(@Valid @RequestBody ShiftRequest request) {
        return ApiResponse.<ShiftResponse>builder()
                .result(shiftService.create(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<ShiftResponse>> getAll() {
        return ApiResponse.<List<ShiftResponse>>builder()
                .result(shiftService.getAll())
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<ShiftResponse> getById(@PathVariable Long id) {
        return ApiResponse.<ShiftResponse>builder()
                .result(shiftService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<ShiftResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ShiftUpdateRequest request
    ) {
        return ApiResponse.<ShiftResponse>builder()
                .result(shiftService.update(id, request))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        shiftService.delete(id);
        return ApiResponse.<String>builder()
                .message("Xóa ca làm việc thành công")
                .build();
    }
}
