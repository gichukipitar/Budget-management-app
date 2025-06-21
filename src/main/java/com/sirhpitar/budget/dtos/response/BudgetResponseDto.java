package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link com.sirhpitar.budget.entities.Budget}
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BudgetResponseDto {
    private Long id;
    private String month;
    private Long userId;
}