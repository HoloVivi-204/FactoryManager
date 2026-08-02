package com.factory.management.modules.masterdata.controller;

import com.factory.management.modules.masterdata.dto.request.TeamLeaderRequest;
import com.factory.management.modules.masterdata.dto.request.TeamRequest;
import com.factory.management.modules.masterdata.dto.request.TeamUpdateRequest;
import com.factory.management.common.api.ApiResponse;
import com.factory.management.modules.masterdata.dto.response.TeamResponse;
import com.factory.management.modules.masterdata.service.TeamService;
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
@RequestMapping("${api.prefix}/teams")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TeamController {
    TeamService teamService;

    @PostMapping
    ApiResponse<TeamResponse> create(@Valid @RequestBody TeamRequest request) {
        return ApiResponse.<TeamResponse>builder()
                .result(teamService.create(request))
                .build();
    }

    @GetMapping("/all")
    ApiResponse<List<TeamResponse>> getAll() {
        return ApiResponse.<List<TeamResponse>>builder()
                .result(teamService.getAll())
                .build();
    }

    @GetMapping("/production-line/{productionLineId}")
    ApiResponse<List<TeamResponse>> getAllByProductionLineId(@PathVariable Long productionLineId) {
        return ApiResponse.<List<TeamResponse>>builder()
                .result(teamService.getAllByProductionLineId(productionLineId))
                .build();
    }

    @GetMapping("/{id}")
    ApiResponse<TeamResponse> getById(@PathVariable Long id) {
        return ApiResponse.<TeamResponse>builder()
                .result(teamService.getById(id))
                .build();
    }

    @PutMapping("/{id}")
    ApiResponse<TeamResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody TeamUpdateRequest request
    ) {
        return ApiResponse.<TeamResponse>builder()
                .result(teamService.update(id, request))
                .build();
    }

    @PutMapping("/{id}/leader")
    ApiResponse<TeamResponse> assignLeader(
            @PathVariable Long id,
            @Valid @RequestBody TeamLeaderRequest request
    ) {
        return ApiResponse.<TeamResponse>builder()
                .result(teamService.assignLeader(id, request.getEmployeeId()))
                .build();
    }

    @DeleteMapping("/{id}/leader")
    ApiResponse<TeamResponse> removeLeader(@PathVariable Long id) {
        return ApiResponse.<TeamResponse>builder()
                .result(teamService.removeLeader(id))
                .build();
    }

    @DeleteMapping("/{id}")
    ApiResponse<String> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ApiResponse.<String>builder()
                .message("Xóa tổ sản xuất thành công")
                .build();
    }
}
