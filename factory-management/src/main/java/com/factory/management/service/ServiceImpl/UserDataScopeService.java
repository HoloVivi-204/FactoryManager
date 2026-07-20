package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.UserDataScopeRequest;
import com.factory.management.dto.response.UserDataScopeResponse;
import com.factory.management.entity.User;
import com.factory.management.entity.UserDataScope;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.repository.DepartmentRepository;
import com.factory.management.repository.FactoryRepository;
import com.factory.management.repository.ProductionLineRepository;
import com.factory.management.repository.TeamRepository;
import com.factory.management.repository.UserDataScopeRepository;
import com.factory.management.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserDataScopeService {
    private final UserDataScopeRepository repository;
    private final UserRepository users;
    private final FactoryRepository factories;
    private final DepartmentRepository departments;
    private final ProductionLineRepository lines;
    private final TeamRepository teams;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<UserDataScopeResponse> getAll(Long userId) {
        users.findById(userId).orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        return repository.findAllByUser_Id(userId).stream().map(this::response).toList();
    }

    @Transactional
    public UserDataScopeResponse add(Long userId, UserDataScopeRequest request) {
        User user = users.findById(userId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        validate(request);
        if (repository.existsByUser_IdAndScopeTypeAndScopeId(
                userId, request.getScopeType(), request.getScopeId())) {
            return repository.findAllByUser_Id(userId).stream()
                    .filter(value -> value.getScopeType() == request.getScopeType()
                            && value.getScopeId().equals(request.getScopeId()))
                    .findFirst()
                    .map(this::response)
                    .orElseThrow();
        }

        UserDataScope saved = repository.save(UserDataScope.builder()
                .user(user)
                .scopeType(request.getScopeType())
                .scopeId(request.getScopeId())
                .build());
        auditService.record("USER_DATA_SCOPE_GRANTED", "USER_DATA_SCOPE", saved.getId(),
                "{\"userId\":" + userId
                        + ",\"scopeType\":\"" + saved.getScopeType().name()
                        + "\",\"scopeId\":" + saved.getScopeId() + "}");
        return response(saved);
    }

    @Transactional
    public void delete(Long userId, Long id) {
        UserDataScope value = repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DATA_SCOPE_NOT_FOUND));
        if (!value.getUser().getId().equals(userId)) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }
        String details = "{\"userId\":" + userId
                + ",\"scopeType\":\"" + value.getScopeType().name()
                + "\",\"scopeId\":" + value.getScopeId() + "}";
        repository.delete(value);
        auditService.record("USER_DATA_SCOPE_REVOKED", "USER_DATA_SCOPE", id, details);
    }

    private void validate(UserDataScopeRequest request) {
        boolean exists = switch (request.getScopeType()) {
            case FACTORY -> factories.existsById(request.getScopeId());
            case DEPARTMENT -> departments.existsById(request.getScopeId());
            case PRODUCTION_LINE -> lines.existsById(request.getScopeId());
            case TEAM -> teams.existsById(request.getScopeId());
        };
        if (!exists) {
            throw new AppException(ErrorCode.DATA_SCOPE_NOT_FOUND);
        }
    }

    private UserDataScopeResponse response(UserDataScope value) {
        String code = "";
        String name = "";
        switch (value.getScopeType()) {
            case FACTORY -> {
                var target = factories.findById(value.getScopeId()).orElse(null);
                if (target != null) {
                    code = target.getCode();
                    name = target.getName();
                }
            }
            case DEPARTMENT -> {
                var target = departments.findById(value.getScopeId()).orElse(null);
                if (target != null) {
                    code = target.getCode();
                    name = target.getName();
                }
            }
            case PRODUCTION_LINE -> {
                var target = lines.findById(value.getScopeId()).orElse(null);
                if (target != null) {
                    code = target.getCode();
                    name = target.getName();
                }
            }
            case TEAM -> {
                var target = teams.findById(value.getScopeId()).orElse(null);
                if (target != null) {
                    code = target.getCode();
                    name = target.getName();
                }
            }
        }
        return UserDataScopeResponse.builder()
                .id(value.getId())
                .userId(value.getUser().getId())
                .scopeType(value.getScopeType())
                .scopeId(value.getScopeId())
                .scopeCode(code)
                .scopeName(name)
                .build();
    }
}
