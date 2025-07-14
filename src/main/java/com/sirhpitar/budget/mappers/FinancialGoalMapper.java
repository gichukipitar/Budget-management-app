package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.FinancialGoalRequestDto;
import com.sirhpitar.budget.dtos.response.FinancialGoalResponseDto;
import com.sirhpitar.budget.entities.FinancialGoal;
import com.sirhpitar.budget.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface FinancialGoalMapper {

    @Mapping(target = "userId", source = "user.id")
    FinancialGoalResponseDto toDto(FinancialGoal financialGoal);

    default FinancialGoal toDto (FinancialGoalRequestDto dto, User user) {
        FinancialGoal goal = toEntity(dto);
        goal.setUser(user);
        return goal;
    }

    FinancialGoal toEntity(FinancialGoalRequestDto dto);

}
