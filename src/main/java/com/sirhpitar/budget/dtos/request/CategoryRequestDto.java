package com.sirhpitar.budget.dtos.request;

import com.sirhpitar.budget.entities.Budget;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO for {@link com.sirhpitar.budget.entities.Category}
 */
@AllArgsConstructor
@Getter
public class CategoryRequestDto {
    private Budget budget;
    private Double allocatedAmount;
    private Double remainingAmount;
}