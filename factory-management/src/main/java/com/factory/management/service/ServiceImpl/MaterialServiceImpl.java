package com.factory.management.service.ServiceImpl;

import com.factory.management.dto.request.MaterialRequest;
import com.factory.management.dto.request.MaterialUpdateRequest;
import com.factory.management.dto.response.MaterialResponse;
import com.factory.management.entity.Material;
import com.factory.management.exception.AppException;
import com.factory.management.exception.ErrorCode;
import com.factory.management.mapper.MaterialMapper;
import com.factory.management.repository.MaterialRepository;
import com.factory.management.service.Service.MaterialService;
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
public class MaterialServiceImpl implements MaterialService {
    MaterialRepository materialRepository;
    MaterialMapper materialMapper;

    @Override
    @Transactional
    public MaterialResponse create(MaterialRequest request) {
        String normalizedCode = normalizeCode(request.getCode());

        if (materialRepository.existsByCodeIgnoreCase(normalizedCode)) {
            throw new AppException(ErrorCode.MATERIAL_CODE_EXISTS);
        }

        Material material = materialMapper.mapToMaterial(request);
        material.setCode(normalizedCode);
        material.setName(request.getName().trim());
        material.setUnit(request.getUnit().trim());
        material.setDescription(trimToNull(request.getDescription()));

        if (material.getActive() == null) {
            material.setActive(true);
        }

        return materialMapper.mapToMaterialResponse(saveMaterial(material));
    }

    @Override
    @Transactional(readOnly = true)
    public List<MaterialResponse> getAll() {
        return materialRepository.findAllByActiveTrue().stream()
                .map(materialMapper::mapToMaterialResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public MaterialResponse getById(Long id) {
        return materialMapper.mapToMaterialResponse(findActiveMaterial(id));
    }

    @Override
    @Transactional
    public MaterialResponse update(Long id, MaterialUpdateRequest request) {
        Material material = findMaterialForUpdate(id);
        String normalizedCode = request.getCode() == null
                ? material.getCode()
                : normalizeCode(request.getCode());

        if (materialRepository.existsByCodeIgnoreCaseAndIdNot(normalizedCode, id)) {
            throw new AppException(ErrorCode.MATERIAL_CODE_EXISTS);
        }

        materialMapper.updateMaterialFromRequest(request, material);
        material.setCode(normalizedCode);

        if (request.getName() != null) {
            material.setName(request.getName().trim());
        }
        if (request.getUnit() != null) {
            material.setUnit(request.getUnit().trim());
        }
        if (request.getDescription() != null) {
            material.setDescription(trimToNull(request.getDescription()));
        }

        return materialMapper.mapToMaterialResponse(saveMaterial(material));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Material material = findActiveMaterial(id);
        material.setActive(false);
    }

    private Material findActiveMaterial(Long id) {
        return materialRepository.findByIdAndActiveTrue(id)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_ID_NOT_FOUND));
    }

    private Material findMaterialForUpdate(Long id) {
        return materialRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.MATERIAL_ID_NOT_FOUND));
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

    private Material saveMaterial(Material material) {
        try {
            return materialRepository.saveAndFlush(material);
        } catch (DataIntegrityViolationException exception) {
            throw new AppException(ErrorCode.MATERIAL_CODE_EXISTS);
        }
    }
}
