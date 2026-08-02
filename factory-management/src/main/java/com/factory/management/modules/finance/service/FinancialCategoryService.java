package com.factory.management.modules.finance.service;

import com.factory.management.modules.finance.dto.request.FinancialCategoryRequest;
import com.factory.management.modules.finance.dto.response.FinancialCategoryResponse;
import com.factory.management.modules.finance.entity.ExpenseGroup;
import com.factory.management.modules.finance.entity.FinancialCategory;
import com.factory.management.modules.finance.entity.FinancialEntryType;
import com.factory.management.common.error.AppException;
import com.factory.management.common.error.ErrorCode;
import com.factory.management.modules.finance.repository.FinancialCategoryRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class FinancialCategoryService {
    private final FinancialCategoryRepository repository;

    @Transactional
    public FinancialCategoryResponse create(FinancialCategoryRequest request) {
        String code = request.getCode().trim().toUpperCase();
        if (repository.existsByCodeIgnoreCase(code)) {
            throw new AppException(ErrorCode.FINANCIAL_CATEGORY_CODE_EXISTS);
        }

        validate(request);

        return response(repository.save(entity(new FinancialCategory(), request, code)));
    }

    @Transactional(readOnly = true)
    public List<FinancialCategoryResponse> all() {
        return repository.findAllByActiveTrueOrderByCode()
                .stream()
                .map(this::response)
                .toList();
    }

    @Transactional
    public FinancialCategoryResponse update(Long id, FinancialCategoryRequest request) {
        FinancialCategory category = find(id);
        String code = request.getCode().trim().toUpperCase();
        if (repository.existsByCodeIgnoreCaseAndIdNot(code, id)) {
            throw new AppException(ErrorCode.FINANCIAL_CATEGORY_CODE_EXISTS);
        }

        validate(request);

        return response(entity(category, request, code));
    }

    @Transactional
    public void delete(Long id) {
        find(id).setActive(false);
    }

    private void validate(FinancialCategoryRequest request) {
        if (request.getEntryType() == FinancialEntryType.REVENUE && request.getExpenseGroup() != null) {
            throw new AppException(ErrorCode.INVALID_FINANCIAL_CATEGORY_TYPE);
        }
    }

    private FinancialCategory entity(FinancialCategory category, FinancialCategoryRequest request, String code) {
        category.setCode(code);
        category.setName(request.getName().trim());
        category.setEntryType(request.getEntryType());
        category.setExpenseGroup(expenseGroup(request));
        category.setDescription(request.getDescription());
        if (request.getActive() != null) {
            category.setActive(request.getActive());
        }

        return category;
    }

    private ExpenseGroup expenseGroup(FinancialCategoryRequest request) {
        return request.getEntryType() == FinancialEntryType.EXPENSE ? request.getExpenseGroup() : null;
    }

    private FinancialCategory find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.FINANCIAL_CATEGORY_NOT_FOUND));
    }

    private FinancialCategoryResponse response(FinancialCategory category) {
        return FinancialCategoryResponse.builder()
                .id(category.getId())
                .code(category.getCode())
                .name(category.getName())
                .entryType(category.getEntryType())
                .expenseGroup(category.getExpenseGroup())
                .description(category.getDescription())
                .active(category.getActive())
                .build();
    }
}
