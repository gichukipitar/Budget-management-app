package com.sirhpitar.budget.dtos.request;

import jakarta.validation.constraints.NotBlank;
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
    @NotBlank(message = "Category name cannot be blank")
    private String categoryName;
    private Double allocatedAmount;
    private Double remainingAmount;
}