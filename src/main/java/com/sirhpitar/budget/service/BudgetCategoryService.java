package com.sirhpitar.budget.service;

import com.sirhpitar.budget.dtos.request.BudgetCategoryRequestDto;
import com.sirhpitar.budget.dtos.response.BudgetCategoryResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BudgetCategoryService {
    Mono<BudgetCategoryResponseDto> createCategory(BudgetCategoryRequestDto dto);

    Mono<BudgetCategoryResponseDto> updateCategory(Long id, BudgetCategoryRequestDto dto);

    Mono<Void> deleteCategory(Long id);

    Mono<BudgetCategoryResponseDto> getCategoryById(Long id);

    Flux<BudgetCategoryResponseDto> getAllCategories();
}
