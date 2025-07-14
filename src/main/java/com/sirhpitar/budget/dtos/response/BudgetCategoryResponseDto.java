package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Builder
@Getter
@Setter
public class BudgetCategoryResponseDto {
    private Long id;
    private Long budgetId;
    private Long expenseCategoryId;
    private Double allocatedAmount;
    private Double alertThreshold;
}