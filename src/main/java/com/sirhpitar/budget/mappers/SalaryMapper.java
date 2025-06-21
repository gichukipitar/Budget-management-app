package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.SalaryRequestDto;
import com.sirhpitar.budget.dtos.response.SalaryResponseDto;
import com.sirhpitar.budget.entities.Salary;
import com.sirhpitar.budget.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface SalaryMapper {

    Salary toEntity(SalaryRequestDto dto);

    @Mapping(target = "userId", source = "user.id")
    SalaryResponseDto toDto(Salary salary);

    default Salary toEntity(SalaryRequestDto dto, User user) {
        Salary salary = toEntity(dto);
        salary.setUser(user);
        return salary;
    }
}