package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.dtos.request.BudgetCategoryRequestDto;
import com.sirhpitar.budget.dtos.response.BudgetCategoryResponseDto;
import com.sirhpitar.budget.entities.Budget;
import com.sirhpitar.budget.entities.BudgetCategory;
import com.sirhpitar.budget.entities.ExpenseCategory;
import com.sirhpitar.budget.exceptions.NotFoundException;
import com.sirhpitar.budget.mappers.BudgetCategoryMapper;
import com.sirhpitar.budget.repository.BudgetCategoryRepository;
import com.sirhpitar.budget.repository.BudgetRepository;
import com.sirhpitar.budget.repository.ExpenseCategoryRepository;
import com.sirhpitar.budget.service.BudgetCategoryService;
import com.sirhpitar.budget.utils.ReactorBlocking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetCategoryServiceImpl implements BudgetCategoryService {

    private final BudgetCategoryRepository budgetCategoryRepository;
    private final BudgetCategoryMapper budgetCategoryMapper;
    private final BudgetRepository budgetRepository;
    private final ExpenseCategoryRepository expenseCategoryRepository;

    @Override
    public Mono<BudgetCategoryResponseDto> createCategory(BudgetCategoryRequestDto dto) {
        return ReactorBlocking.mono(() -> createBudgetCategory(dto));
    }

    @Override
    public Mono<BudgetCategoryResponseDto> updateCategory(Long id, BudgetCategoryRequestDto dto) {
        return ReactorBlocking.mono(() -> updateBudgetCategory(id, dto));
    }

    @Override
    public Mono<Void> deleteCategory(Long id) {
        return ReactorBlocking.run(() -> {
            if (!budgetCategoryRepository.existsById(id)) {
                throw new NotFoundException("Budget category not found");
            }
            budgetCategoryRepository.deleteById(id);
        });
    }

    @Override
    public Mono<BudgetCategoryResponseDto> getCategoryById(Long id) {
        return ReactorBlocking.mono(() -> budgetCategoryRepository.findById(id))
                .flatMap(optionalCategory -> optionalCategory
                        .map(category -> Mono.just(budgetCategoryMapper.toDto(category)))
                        .orElseGet(Mono::empty));
    }

    @Override
    public Flux<BudgetCategoryResponseDto> getAllCategories() {
        return ReactorBlocking.mono(budgetCategoryRepository::findAll)
                .flatMapMany(Flux::fromIterable)
                .map(budgetCategoryMapper::toDto);
    }

    // --- Private helper methods ---

    private Budget findBudgetOrThrow(Long budgetId) {
        return budgetRepository.findById(budgetId)
                .orElseThrow(() -> new NotFoundException("Budget not found"));
    }

    private ExpenseCategory findExpenseCategoryOrThrow(Long expenseCategoryId) {
        return expenseCategoryRepository.findById(expenseCategoryId)
                .orElseThrow(() -> new NotFoundException("Expense category not found"));
    }

    private BudgetCategory findBudgetCategoryOrThrow(Long id) {
        return budgetCategoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Budget category not found"));
    }

    private BudgetCategoryResponseDto createBudgetCategory(BudgetCategoryRequestDto dto) {
        Budget budget = findBudgetOrThrow(dto.getBudgetId());
        ExpenseCategory expenseCategory = findExpenseCategoryOrThrow(dto.getExpenseCategoryId());

        BudgetCategory budgetCategory = budgetCategoryMapper.toEntity(dto, budget, expenseCategory);
        BudgetCategory savedCategory = budgetCategoryRepository.save(budgetCategory);

        return budgetCategoryMapper.toDto(savedCategory);
    }

    private BudgetCategoryResponseDto updateBudgetCategory(Long id, BudgetCategoryRequestDto dto) {
        BudgetCategory budgetCategory = findBudgetCategoryOrThrow(id);

        budgetCategoryMapper.updateBudgetCategoryFromDto(dto, budgetCategory);

        if (dto.getBudgetId() != null
                && (budgetCategory.getBudget() == null || !dto.getBudgetId().equals(budgetCategory.getBudget().getId()))) {
            budgetCategory.setBudget(findBudgetOrThrow(dto.getBudgetId()));
        }

        if (dto.getExpenseCategoryId() != null
                && (budgetCategory.getExpenseCategory() == null
                || !dto.getExpenseCategoryId().equals(budgetCategory.getExpenseCategory().getId()))) {
            budgetCategory.setExpenseCategory(findExpenseCategoryOrThrow(dto.getExpenseCategoryId()));
        }

        BudgetCategory savedCategory = budgetCategoryRepository.save(budgetCategory);
        return budgetCategoryMapper.toDto(savedCategory);
    }
}
