package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO for {@link com.sirhpitar.budget.entities.Budget}
 */
@AllArgsConstructor
@Getter
public class BudgetResponseDto {
    private String month;
}