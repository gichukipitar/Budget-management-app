package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.ExpenseRequestDto;
import com.sirhpitar.budget.dtos.response.ExpenseResponseDto;
import com.sirhpitar.budget.entities.Category;
import com.sirhpitar.budget.entities.Expense;
import com.sirhpitar.budget.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpenseMapper {

    Expense toEntity(ExpenseRequestDto dto);

    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "userId", source = "user.id")
    ExpenseResponseDto toDto(Expense expense);

    default Expense toEntity(ExpenseRequestDto dto, Category category, User user) {
        Expense expense = toEntity(dto);
        expense.setCategory(category);
        expense.setUser(user);
        return expense;
    }
}
