package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.BudgetCategoryRequestDto;
import com.sirhpitar.budget.dtos.response.BudgetCategoryResponseDto;
import com.sirhpitar.budget.entities.Budget;
import com.sirhpitar.budget.entities.BudgetCategory;
import com.sirhpitar.budget.entities.ExpenseCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BudgetCategoryMapper {

    @Mapping(target = "budgetId", source = "budget.id")
    @Mapping(target = "expenseCategoryId", source = "expenseCategory.id")
    BudgetCategoryResponseDto toDto(BudgetCategory category);

    default BudgetCategory toEntity(BudgetCategoryRequestDto dto, Budget budget, ExpenseCategory expenseCategory) {
        BudgetCategory category = toEntity(dto);
        category.setBudget(budget);
        category.setExpenseCategory(expenseCategory);
        return category;
    }

    @Mapping(target = "budget", ignore = true)
    @Mapping(target = "expenseCategory", ignore = true)
    BudgetCategory toEntity(BudgetCategoryRequestDto dto);

    // In-place update method (ignores id, budget, expenseCategory)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "budget", ignore = true)
    @Mapping(target = "expenseCategory", ignore = true)
    void updateBudgetCategoryFromDto(BudgetCategoryRequestDto dto, @MappingTarget BudgetCategory category);
}
