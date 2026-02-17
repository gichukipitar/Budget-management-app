package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.UserResponseDto;
import com.sirhpitar.budget.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Create entity from DTO (for registration/admin creation)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "budgets", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "expenseCategories", ignore = true)
    @Mapping(target = "incomeSources", ignore = true)
    @Mapping(target = "financialGoals", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    User toEntity(UserRequestDto dto);

    // Safe in-place update (VERY important)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "password", ignore = true) // password handled manually
    @Mapping(target = "budgets", ignore = true)
    @Mapping(target = "expenses", ignore = true)
    @Mapping(target = "expenseCategories", ignore = true)
    @Mapping(target = "incomeSources", ignore = true)
    @Mapping(target = "financialGoals", ignore = true)
    @Mapping(target = "notifications", ignore = true)
    void updateUserFromDto(UserRequestDto dto, @MappingTarget User user);

    UserResponseDto toDto(User user);
}
