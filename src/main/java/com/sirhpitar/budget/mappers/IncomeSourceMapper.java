package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.IncomeSourceRequestDto;
import com.sirhpitar.budget.dtos.response.IncomeSourceResponseDto;
import com.sirhpitar.budget.entities.IncomeSource;
import com.sirhpitar.budget.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface IncomeSourceMapper {

    @Mapping(target = "userId", source = "user.id")
    IncomeSourceResponseDto toEntity(IncomeSource incomeSource);

    default IncomeSource toEntity(IncomeSourceRequestDto dto, User user) {
        IncomeSource incomeSource = toEntity(dto);
        incomeSource.setUser(user);
        return incomeSource;

    }

    IncomeSource toEntity(IncomeSourceRequestDto dto);
}
