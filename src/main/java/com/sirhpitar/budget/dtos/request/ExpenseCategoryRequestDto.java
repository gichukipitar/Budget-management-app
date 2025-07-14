package com.sirhpitar.budget.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class ExpenseCategoryRequestDto {
    private String name;
    private String color;
    private String icon;
    private Long parentCategoryId;
}
