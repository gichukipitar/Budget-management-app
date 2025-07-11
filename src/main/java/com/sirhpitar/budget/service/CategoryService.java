package com.sirhpitar.budget.service;

import com.sirhpitar.budget.dtos.response.CategoryResponseDto;
import reactor.core.publisher.Mono;

public interface CategoryService {
    Mono<CategoryResponseDto> createCategory(CategoryResponseDto dto);

    Mono<CategoryResponseDto> updateCategory(Long id, CategoryResponseDto dto);

    Mono<Void> deleteCategory(Long id);
}
