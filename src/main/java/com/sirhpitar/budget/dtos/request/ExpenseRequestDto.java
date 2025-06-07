package com.sirhpitar.budget.dtos.request;

import com.sirhpitar.budget.entities.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;

/**
 * DTO for {@link com.sirhpitar.budget.entities.Expense}
 */
@AllArgsConstructor
@Getter
public class ExpenseRequestDto {
    private Category category;
    private Double amount;
    private String description;
    private LocalDate date;
}