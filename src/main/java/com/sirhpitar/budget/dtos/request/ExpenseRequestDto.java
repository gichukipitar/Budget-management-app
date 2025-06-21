package com.sirhpitar.budget.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/**
 * DTO for {@link com.sirhpitar.budget.entities.Expense}
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ExpenseRequestDto {
    private Long categoryId;
    private Long userId;
    private Double amount;
    private String description;
    private LocalDate date;
}