package com.factory.management.modules.masterdata.service;

import com.factory.management.common.api.PageResponse;
import com.factory.management.common.error.AppException;
import com.factory.management.common.error.ErrorCode;
import com.factory.management.common.security.CurrentUserService;
import com.factory.management.modules.audit.service.AuditService;
import com.factory.management.modules.masterdata.dto.request.ProductRequest;
import com.factory.management.modules.masterdata.dto.response.ProductResponse;
import com.factory.management.modules.masterdata.entity.Product;
import com.factory.management.modules.masterdata.repository.ProductRepository;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private static final int MAX_PAGE_SIZE = 200;

    private final ProductRepository productRepository;
    private final CurrentUserService currentUserService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public PageResponse<ProductResponse> search(
            String keyword,
            Boolean active,
            int page,
            int size
    ) {
        Specification<Product> specification = (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (active != null) predicates.add(cb.equal(root.get("active"), active));
            if (keyword != null && !keyword.isBlank()) {
                String pattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("code")), pattern),
                        cb.like(cb.lower(root.get("name")), pattern)
                ));
            }
            return cb.and(predicates.toArray(Predicate[]::new));
        };
        var result = productRepository.findAll(specification,
                PageRequest.of(safePage(page), safeSize(size), Sort.by(Sort.Direction.ASC, "code")));
        return PageResponse.from(result, this::response);
    }

    @Transactional(readOnly = true)
    public ProductResponse get(Long id) {
        return response(find(id));
    }

    @Transactional
    public ProductResponse create(ProductRequest request) {
        String code = normalizeCode(request.getCode());
        if (productRepository.existsByCodeIgnoreCase(code)) {
            throw new AppException(ErrorCode.PRODUCT_CODE_EXISTS);
        }
        Product value = productRepository.save(Product.builder()
                .code(code)
                .name(request.getName().trim())
                .unit(request.getUnit().trim())
                .standardCycleSeconds(request.getStandardCycleSeconds())
                .active(request.getActive() == null || request.getActive())
                .createdBy(currentUserService.user().getUsername())
                .build());
        auditService.record("CREATE", "PRODUCT", value.getId(),
                "{\"code\":\"" + escape(code) + "\"}");
        return response(value);
    }

    @Transactional
    public ProductResponse update(Long id, ProductRequest request) {
        Product value = find(id);
        String code = normalizeCode(request.getCode());
        if (productRepository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new AppException(ErrorCode.PRODUCT_CODE_EXISTS);
        }
        value.setCode(code);
        value.setName(request.getName().trim());
        value.setUnit(request.getUnit().trim());
        value.setStandardCycleSeconds(request.getStandardCycleSeconds());
        if (request.getActive() != null) value.setActive(request.getActive());
        auditService.record("UPDATE", "PRODUCT", value.getId(),
                "{\"code\":\"" + escape(code) + "\"}");
        return response(value);
    }

    @Transactional
    public void delete(Long id) {
        Product value = find(id);
        value.setActive(false);
        auditService.record("SOFT_DELETE", "PRODUCT", value.getId(), null);
    }

    private Product find(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private ProductResponse response(Product value) {
        return ProductResponse.builder()
                .id(value.getId())
                .code(value.getCode())
                .name(value.getName())
                .unit(value.getUnit())
                .standardCycleSeconds(value.getStandardCycleSeconds())
                .active(value.getActive())
                .createdBy(value.getCreatedBy())
                .createdAt(value.getCreatedAt())
                .updatedAt(value.getUpdatedAt())
                .version(value.getVersion())
                .build();
    }

    private String normalizeCode(String value) {
        return value.trim().toUpperCase(Locale.ROOT);
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private int safePage(int value) {
        return Math.max(value, 0);
    }

    private int safeSize(int value) {
        return Math.min(Math.max(value, 1), MAX_PAGE_SIZE);
    }
}
