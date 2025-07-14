package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.ExpenseRequestDto;
import com.sirhpitar.budget.dtos.response.ExpenseResponseDto;
import com.sirhpitar.budget.entities.Expense;
import com.sirhpitar.budget.entities.ExpenseCategory;
import com.sirhpitar.budget.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.BeanMapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    @Mapping(target = "expenseCategory.id", source = "expenseCategoryId")
    @Mapping(target = "user.id", source = "userId")
    Expense toEntity(ExpenseRequestDto dto);

    @Mapping(target = "expenseCategoryId", source = "expenseCategory.id")
    @Mapping(target = "userId", source = "user.id")
    ExpenseResponseDto toDto(Expense expense);

    // Create expense with category and user
    default Expense toEntity(ExpenseRequestDto dto, ExpenseCategory expenseCategory, User user) {
        Expense expense = toEntity(dto);
        expense.setExpenseCategory(expenseCategory);
        expense.setUser(user);
        return expense;
    }

    // Update existing expense - only updates non-null fields
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateExpenseFromDto(ExpenseRequestDto dto, @MappingTarget Expense expense);

    // Update expense with category and user
    default void updateExpense(ExpenseRequestDto dto, @MappingTarget Expense expense,
                               ExpenseCategory expenseCategory, User user) {
        updateExpenseFromDto(dto, expense);
        if (expenseCategory != null) {
            expense.setExpenseCategory(expenseCategory);
        }

        if (user != null) {
            expense.setUser(user);
        }
    }
}