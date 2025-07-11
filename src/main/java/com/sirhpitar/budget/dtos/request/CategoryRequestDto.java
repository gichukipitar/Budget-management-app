package com.sirhpitar.budget.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link com.sirhpitar.budget.entities.Category}
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CategoryRequestDto {
    private Long budgetId;
    private String name;
    private Double allocatedAmount;
    private Double remainingAmount;
}