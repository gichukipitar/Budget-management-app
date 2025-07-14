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
    private Long expenseCategoryId;
    private Double amount;
    private String description;
    private LocalDate transactionDate;
    private String paymentMethod;
    private String receiptUrl;
    private boolean recurring;
    private String recurringFrequency;
    private Long userId;
}