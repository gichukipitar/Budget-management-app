package com.sirhpitar.budget.dtos.response;

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
public class ExpenseResponseDto {
    private Long id;
    private Long categoryId;
    private Long userId;
    private Double amount;
    private String description;
    private LocalDate date;
}