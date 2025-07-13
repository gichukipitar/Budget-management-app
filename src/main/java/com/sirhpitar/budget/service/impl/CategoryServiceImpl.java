package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.dtos.request.CategoryRequestDto;
import com.sirhpitar.budget.dtos.response.CategoryResponseDto;
import com.sirhpitar.budget.entities.Budget;
import com.sirhpitar.budget.entities.Category;
import com.sirhpitar.budget.exceptions.NotFoundException;
import com.sirhpitar.budget.mappers.CategoryMapper;
import com.sirhpitar.budget.repository.BudgetRepository;
import com.sirhpitar.budget.repository.CategoryRepository;
import com.sirhpitar.budget.service.CategoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
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
                        return Mono.error(new NotFoundException("Budget not found"));
                    }
                    Budget budget = optionalBudget.get();

                    // Log values before duplicate check
                    log.info("Checking for duplicate: categoryName={}, budgetId={}", dto.getCategoryName(), dto.getBudgetId());

                    // Check for duplicate category name for this budget
                    return Mono.fromCallable(() -> categoryRepository.findByCategoryNameAndBudgetId(dto.getCategoryName(), dto.getBudgetId()))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(existingCategory -> {
                                log.info("Duplicate exists? {}", existingCategory.isPresent());
                                if (existingCategory.isPresent()) {
                                    return Mono.error(new IllegalArgumentException("Category name already exists for this budget"));
                                }

                                // Log values before insert
                                log.info("Inserting category: categoryName={}, budgetId={}, allocatedAmount={}, remainingAmount={}",
                                        dto.getCategoryName(), dto.getBudgetId(), dto.getAllocatedAmount(), dto.getRemainingAmount());

                                Category category = categoryMapper.toEntity(dto, budget);
                                return Mono.fromCallable(() -> categoryRepository.save(category))
                                        .subscribeOn(Schedulers.boundedElastic())
                                        .map(categoryMapper::toDto);
                            });
                });
    }

    @Override
    public Mono<CategoryResponseDto> updateCategory(Long id, CategoryRequestDto dto) {
        return Mono.fromCallable(() -> categoryRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalCategory -> {
                    if (optionalCategory.isEmpty()) {
                        return Mono.error(new NotFoundException("Category not found"));
                    }
                    Category category = optionalCategory.get();

                    // Check for name change and possible duplicate
                    if (!category.getCategoryName().equals(dto.getCategoryName()) ||
                            (dto.getBudgetId() != null && !dto.getBudgetId().equals(category.getBudget().getId()))) {

                        Long budgetIdToCheck = dto.getBudgetId() != null ? dto.getBudgetId() : category.getBudget().getId();
                        return Mono.fromCallable(() -> categoryRepository.findByCategoryNameAndBudgetId(dto.getCategoryName(), budgetIdToCheck))
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMap(existingCategory -> {
                                    if (existingCategory.isPresent() && !existingCategory.get().getId().equals(id)) {
                                        return Mono.error(new IllegalArgumentException("Category name already exists for this budget"));
                                    }
                                    return updateCategoryEntity(category, dto);
                                });
                    } else {
                        return updateCategoryEntity(category, dto);
                    }
                });
    }

    private Mono<CategoryResponseDto> updateCategoryEntity(Category category, CategoryRequestDto dto) {
        categoryMapper.updateEntity(category, dto);

        // Optionally update budget if provided and different
        if (dto.getBudgetId() != null && !dto.getBudgetId().equals(category.getBudget().getId())) {
            return Mono.fromCallable(() -> budgetRepository.findById(dto.getBudgetId()))
                    .subscribeOn(Schedulers.boundedElastic())
                    .flatMap(optionalBudget -> {
                        if (optionalBudget.isEmpty()) {
                            return Mono.error(new NotFoundException("Budget not found"));
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
    }

    @Override
    public Mono<Void> deleteCategory(Long id) {
        return Mono.fromCallable(() -> {
            if (!categoryRepository.existsById(id)) {
                throw new NotFoundException("Category not found");
            }
            categoryRepository.deleteById(id);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Mono<CategoryResponseDto> getCategoryById(Long id) {
        return Mono.fromCallable(() -> categoryRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalCategory -> optionalCategory
                        .map(category -> Mono.just(categoryMapper.toDto(category)))
                        .orElseGet(Mono::empty));
    }

    @Override
    public Flux<CategoryResponseDto> getAllCategories() {
        return Mono.fromCallable(categoryRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(categoryMapper::toDto);
    }
}