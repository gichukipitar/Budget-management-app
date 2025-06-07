package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * DTO for {@link com.sirhpitar.budget.entities.Expense}
 */
@AllArgsConstructor
@Getter
public class ExpenseDto {
    private Double amount;
    private String description;
    private LocalDate date;
}