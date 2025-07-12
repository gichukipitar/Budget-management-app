package com.sirhpitar.budget.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO for {@link com.sirhpitar.budget.entities.Budget}
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class BudgetRequestDto {
    private LocalDate budgetDate;
    private Long userId;
}