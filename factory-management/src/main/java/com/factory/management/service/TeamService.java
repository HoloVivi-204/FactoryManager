package com.factory.management.service;

import com.factory.management.dto.request.TeamRequest;
import com.factory.management.dto.request.TeamUpdateRequest;
import com.factory.management.dto.response.TeamResponse;
import java.util.List;

public interface TeamService {
    TeamResponse create(TeamRequest request);

    List<TeamResponse> getAll();

    List<TeamResponse> getAllByProductionLineId(Long productionLineId);

    TeamResponse getById(Long id);

    TeamResponse update(Long id, TeamUpdateRequest request);

    TeamResponse assignLeader(Long id, Long employeeId);

    TeamResponse removeLeader(Long id);

    void delete(Long id);
}
