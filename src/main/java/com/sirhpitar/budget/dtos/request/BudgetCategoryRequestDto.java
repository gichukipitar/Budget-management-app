package com.sirhpitar.budget.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Setter
@Getter
@Builder
public class BudgetCategoryRequestDto {
    private Long budgetId;
    private Long expenseCategoryId;
    private Double allocatedAmount;
    private Double alertThreshold;
}