package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.CategoryRequestDto;
import com.sirhpitar.budget.dtos.response.CategoryResponseDto;
import com.sirhpitar.budget.entities.Budget;
import com.sirhpitar.budget.entities.Category;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface CategoryMapper {

    Category toEntity(CategoryRequestDto dto);

    @Mapping(target = "budgetId", source = "budget.id")
    @Mapping(target = "categoryName", source = "categoryName")
    CategoryResponseDto toDto(Category category);

    default Category toEntity(CategoryRequestDto dto, Budget budget) {
        Category category = toEntity(dto);
        category.setBudget(budget);
        return category;
    }

    void updateEntity(@MappingTarget Category category, CategoryRequestDto dto);
}
