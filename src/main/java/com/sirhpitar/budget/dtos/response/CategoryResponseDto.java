package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class CategoryResponseDto {
    private Long id;
    private Long budgetId;
    private String categoryName;
    private Double allocatedAmount;
    private Double remainingAmount;
}
