package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.dtos.request.CategoryRequestDto;
import com.sirhpitar.budget.dtos.response.CategoryResponseDto;
import com.sirhpitar.budget.entities.Budget;
import com.sirhpitar.budget.entities.Category;
import com.sirhpitar.budget.mappers.CategoryMapper;
import com.sirhpitar.budget.repository.BudgetRepository;
import com.sirhpitar.budget.repository.CategoryRepository;
import com.sirhpitar.budget.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;
    private final BudgetRepository budgetRepository;

    @Override
    public Mono<CategoryResponseDto> createCategory(CategoryRequestDto dto) {
        return Mono.fromCallable(() -> budgetRepository.findById(dto.getBudgetId()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalBudget -> {
                    if (optionalBudget.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("Budget not found"));
                    }
                    Budget budget = optionalBudget.get();
                    Category category = new Category();
                    category.setName(dto.getName());
                    category.setAllocatedAmount(dto.getAllocatedAmount());
                    category.setRemainingAmount(dto.getRemainingAmount());
                    category.setBudget(budget);
                    return Mono.fromCallable(() -> categoryRepository.save(category))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(categoryMapper::toDto);
                });
    }

    @Override
    public Mono<CategoryResponseDto> updateCategory(Long id, CategoryRequestDto dto) {
        return Mono.fromCallable(() -> categoryRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalCategory -> {
                    if (optionalCategory.isEmpty()) {
                        return Mono.error(new IllegalArgumentException("Category not found"));
                    }
                    Category category = optionalCategory.get();
                    category.setName(dto.getName());
                    category.setAllocatedAmount(dto.getAllocatedAmount());
                    category.setRemainingAmount(dto.getRemainingAmount());

                    // Optionally update budget if provided and different
                    if (dto.getBudgetId() != null && !dto.getBudgetId().equals(category.getBudget().getId())) {
                        return Mono.fromCallable(() -> budgetRepository.findById(dto.getBudgetId()))
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMap(optionalBudget -> {
                                    if (optionalBudget.isEmpty()) {
                                        return Mono.error(new IllegalArgumentException("Budget not found"));
                                    }
                                    category.setBudget(optionalBudget.get());
                                    return Mono.fromCallable(() -> categoryRepository.save(category))
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .map(categoryMapper::toDto);
                                });
                    }
                    return Mono.fromCallable(() -> categoryRepository.save(category))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(categoryMapper::toDto);
                });
    }

    @Override
    public Mono<Void> deleteCategory(Long id) {
        return Mono.fromCallable(() -> {
            if (!categoryRepository.existsById(id)) {
                throw new IllegalArgumentException("Category not found");
            }
            categoryRepository.deleteById(id);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }
}
