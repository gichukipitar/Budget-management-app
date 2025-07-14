package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.BudgetRequestDto;
import com.sirhpitar.budget.dtos.response.BudgetResponseDto;
import com.sirhpitar.budget.entities.Budget;
import com.sirhpitar.budget.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface BudgetMapper {


    Budget toEntity(BudgetRequestDto dto);

    @Mapping(target = "userId", source = "user.id")
    BudgetResponseDto toDto(Budget budget);

    // Default method to handle the extra User parameter
    default Budget toEntity(BudgetRequestDto dto, User user) {
        Budget budget = toEntity(dto);
        budget.setUser(user);
        return budget;
    }

    @Mapping(target = "id", ignore = true)
    void updateBudgetFromDto(BudgetRequestDto dto, @MappingTarget Budget budget);
}
