package com.sirhpitar.budget.service;

import com.sirhpitar.budget.dtos.request.CategoryRequestDto;
import com.sirhpitar.budget.dtos.response.CategoryResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface CategoryService {
    Mono<CategoryResponseDto> createCategory(CategoryRequestDto dto);

    Mono<CategoryResponseDto> updateCategory(Long id, CategoryRequestDto dto);

    Mono<Void> deleteCategory(Long id);

    Mono<CategoryResponseDto> getCategoryById(Long id);

    Flux<CategoryResponseDto> getAllCategories();
}
