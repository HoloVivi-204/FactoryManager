package com.factory.management.service.impl;

import com.factory.management.dto.request.DowntimeReasonRequest;
import com.factory.management.dto.request.DowntimeReasonUpdateRequest;
import com.factory.management.dto.response.DowntimeReasonResponse;
import com.factory.management.entity.DowntimeReason;
import com.factory.management.entity.DowntimeReasonType;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.DowntimeReasonMapper;
import com.factory.management.repository.DowntimeReasonRepository;
import com.factory.management.service.DowntimeReasonService;
import java.util.List;
import java.util.Locale;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class DowntimeReasonServiceImpl implements DowntimeReasonService {
    DowntimeReasonRepository downtimeReasonRepository;
    DowntimeReasonMapper downtimeReasonMapper;

    @Override
    @Transactional
    public DowntimeReasonResponse create(DowntimeReasonRequest request) {
        String normalizedCode = normalizeCode(request.getCode());

        if (downtimeReasonRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new AppException(ErrorCode.DOWNTIME_REASON_CODE_EXISTS);
        }

        DowntimeReason downtimeReason = downtimeReasonMapper.mapToDowntimeReason(request);
        downtimeReason.setCode(normalizedCode);
        downtimeReason.setName(request.getName().trim());
        downtimeReason.setDescription(trimToNull(request.getDescription()));

        if (downtimeReason.getActive() == null) {
            downtimeReason.setActive(true);
        }

        return downtimeReasonMapper.mapToDowntimeReasonResponse(saveDowntimeReason(downtimeReason));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DowntimeReasonResponse> getAll() {
        return downtimeReasonRepository.findAllByActiveTrue().stream()
                .map(downtimeReasonMapper::mapToDowntimeReasonResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<DowntimeReasonResponse> getAllByReasonType(DowntimeReasonType reasonType) {
        return downtimeReasonRepository.findAllByReasonTypeAndActiveTrue(reasonType).stream()
                .map(downtimeReasonMapper::mapToDowntimeReasonResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public DowntimeReasonResponse getById(Long id) {
        return downtimeReasonMapper.mapToDowntimeReasonResponse(findActiveDowntimeReason(id));
    }

    @Override
    @Transactional
    public DowntimeReasonResponse update(Long id, DowntimeReasonUpdateRequest request) {
        DowntimeReason downtimeReason = findDowntimeReasonForUpdate(id);
        String normalizedCode = request.getCode() == null
                ? downtimeReason.getCode()
                : normalizeCode(request.getCode());

        if (downtimeReasonRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new AppException(ErrorCode.DOWNTIME_REASON_CODE_EXISTS);
        }

        downtimeReasonMapper.updateDowntimeReasonFromRequest(request, downtimeReason);
        downtimeReason.setCode(normalizedCode);

        if (request.getName() != null) {
            downtimeReason.setName(request.getName().trim());
        }
        if (request.getDescription() != null) {
            downtimeReason.setDescription(trimToNull(request.getDescription()));
        }

        return downtimeReasonMapper.mapToDowntimeReasonResponse(saveDowntimeReason(downtimeReason));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        DowntimeReason downtimeReason = findActiveDowntimeReason(id);
        downtimeReason.setActive(false);
    }

    private DowntimeReason findActiveDowntimeReason(Long id) {
        return downtimeReasonRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOWNTIME_REASON_ID_NOT_FOUND));
    }

    private DowntimeReason findDowntimeReasonForUpdate(Long id) {
        return downtimeReasonRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.DOWNTIME_REASON_ID_NOT_FOUND));
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private String trimToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private DowntimeReason saveDowntimeReason(DowntimeReason downtimeReason) {
        try {
            return downtimeReasonRepository.saveAndFlush(downtimeReason);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.DOWNTIME_REASON_CODE_EXISTS);
        }
    }
}
