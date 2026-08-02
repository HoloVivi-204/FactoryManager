package com.factory.management.service.impl;

import com.factory.management.dto.request.ShiftRequest;
import com.factory.management.dto.request.ShiftUpdateRequest;
import com.factory.management.dto.response.ShiftResponse;
import com.factory.management.entity.Shift;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.ShiftMapper;
import com.factory.management.repository.ShiftRepository;
import com.factory.management.service.ShiftService;
import java.time.LocalTime;
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
public class ShiftServiceImpl implements ShiftService {
    ShiftRepository shiftRepository;
    ShiftMapper shiftMapper;

    @Override
    @Transactional
    public ShiftResponse create(ShiftRequest request) {
        String normalizedCode = normalizeCode(request.getCode());

        if (shiftRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new AppException(ErrorCode.SHIFT_CODE_EXISTS);
        }

        validateTimeRange(request.getStartTime(), request.getEndTime());

        Shift shift = shiftMapper.mapToShift(request);
        shift.setCode(normalizedCode);
        shift.setName(request.getName().trim());

        if (shift.getActive() == null) {
            shift.setActive(true);
        }

        return shiftMapper.mapToShiftResponse(saveShift(shift));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ShiftResponse> getAll() {
        return shiftRepository.findAllByActiveTrue().stream()
                .map(shiftMapper::mapToShiftResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ShiftResponse getById(Long id) {
        return shiftMapper.mapToShiftResponse(findActiveShift(id));
    }

    @Override
    @Transactional
    public ShiftResponse update(Long id, ShiftUpdateRequest request) {
        Shift shift = findShiftForUpdate(id);
        String normalizedCode = request.getCode() == null
                ? shift.getCode()
                : normalizeCode(request.getCode());
        LocalTime startTime = request.getStartTime() == null
                ? shift.getStartTime()
                : request.getStartTime();
        LocalTime endTime = request.getEndTime() == null
                ? shift.getEndTime()
                : request.getEndTime();

        if (shiftRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new AppException(ErrorCode.SHIFT_CODE_EXISTS);
        }

        validateTimeRange(startTime, endTime);
        shiftMapper.updateShiftFromRequest(request, shift);
        shift.setCode(normalizedCode);
        shift.setStartTime(startTime);
        shift.setEndTime(endTime);

        if (request.getName() != null) {
            shift.setName(request.getName().trim());
        }

        return shiftMapper.mapToShiftResponse(saveShift(shift));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Shift shift = findActiveShift(id);
        shift.setActive(false);
    }

    private Shift findActiveShift(Long id) {
        return shiftRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_ID_NOT_FOUND));
    }

    private Shift findShiftForUpdate(Long id) {
        return shiftRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.SHIFT_ID_NOT_FOUND));
    }

    private void validateTimeRange(LocalTime startTime, LocalTime endTime) {
        if (startTime.equals(endTime)) {
            throw new AppException(ErrorCode.INVALID_SHIFT_TIME_RANGE);
        }
    }

    private String normalizeCode(String code) {
        return code.trim().toUpperCase(Locale.ROOT);
    }

    private Shift saveShift(Shift shift) {
        try {
            return shiftRepository.saveAndFlush(shift);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.SHIFT_CODE_EXISTS);
        }
    }
}
