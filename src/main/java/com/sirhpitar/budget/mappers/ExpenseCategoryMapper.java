package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.ExpenseCategoryRequestDto;
import com.sirhpitar.budget.dtos.response.ExpenseCategoryResponseDto;
import com.sirhpitar.budget.entities.ExpenseCategory;
import com.sirhpitar.budget.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ExpenseCategoryMapper {

    ExpenseCategory toEntity(ExpenseCategoryRequestDto dto);

    @Mapping(target = "userId", source = "user.id")
    ExpenseCategoryResponseDto toDto(ExpenseCategory category);

    default ExpenseCategory toEntity(ExpenseCategoryRequestDto dto, User user) {
        ExpenseCategory category = toEntity(dto);
        category.setUser(user);
        return category;
    }
}
