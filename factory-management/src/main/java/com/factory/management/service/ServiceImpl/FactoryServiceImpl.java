package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.FactoryRequest;
import com.factory.management.dto.request.FactoryUpdateRequest;
import com.factory.management.dto.response.FactoryResponse;
import com.factory.management.entity.Factory;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.FactoryMapper;
import com.factory.management.repository.FactoryRepository;
import com.factory.management.service.Service.FactoryService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class FactoryServiceImpl implements FactoryService {
    FactoryRepository factoryRepository;
    FactoryMapper factoryMapper;

    @Override
    @Transactional
    public FactoryResponse create(FactoryRequest request){
        String normalizedCode = request.getCode().trim().toUpperCase(Locale.ROOT);

        if (factoryRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new AppException(ErrorCode.FACTORY_CODE_EXISTS);
        }

        Factory factory = factoryMapper.mapToFactory(request);

        factory.setCode(normalizedCode);
        factory.setName(request.getName().trim());
        factory.setAddress(request.getAddress().trim());

        if (factory.getActive() == null) {
            factory.setActive(true);
        }

        Factory savedFactory = saveFactory(factory);

        return factoryMapper.mapToFactoryResponse(savedFactory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<FactoryResponse> getAll() {
        return factoryRepository.findAllByActiveTrue().stream()
                .map(factoryMapper::mapToFactoryResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public FactoryResponse getById(Long id) {
        return factoryMapper.mapToFactoryResponse(factoryRepository.findByIdAndActiveTrue(id).orElseThrow(
                () -> new AppException(ErrorCode.FACTORY_ID_NOT_FOUND)
        ));
    }

    @Override
    @Transactional
    public FactoryResponse update(Long id, FactoryUpdateRequest request){
        Factory factory = findFactoryById(id);

        String normalizedCode = request.getCode() == null
                ? factory.getCode()
                : request.getCode().trim().toUpperCase(Locale.ROOT);

        if (factoryRepository.existsByCodeIgnoreCaseAndIdNot(
                normalizedCode,
                id
        )) {
            throw new AppException(ErrorCode.FACTORY_CODE_EXISTS);
        }

        factoryMapper.updateFactoryFromRequest(request, factory);

        factory.setCode(normalizedCode);
        if (request.getName() != null) {
            factory.setName(request.getName().trim());
        }
        if (request.getAddress() != null) {
            factory.setAddress(trimToNull(request.getAddress()));
        }

        Factory updatedFactory = saveFactory(factory);

        return factoryMapper.mapToFactoryResponse(updatedFactory);
    }


    @Override
    @Transactional
    public void delete(Long id) {
        Factory factory = findFactoryById(id);
        factory.setActive(false);
    }


    private Factory findFactoryById(Long id){
        return factoryRepository.findByIdAndActiveTrue(id).orElseThrow(
                () -> new AppException(ErrorCode.FACTORY_ID_NOT_FOUND)
        );
    }

    private Factory saveFactory(Factory factory) {
        try {
            return factoryRepository.saveAndFlush(factory);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.FACTORY_CODE_EXISTS);
        }
    }

    private String trimToNull(String value) {
        if (value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
