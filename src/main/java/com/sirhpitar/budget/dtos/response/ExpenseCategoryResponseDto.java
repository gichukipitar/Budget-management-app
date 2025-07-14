package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ExpenseCategoryResponseDto {
    private Long id;
    private String name;
    private String color;
    private String icon;
    private Long parentCategoryId;
    private Long userId;
}