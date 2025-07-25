package com.sirhpitar.budget.dtos.response;

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
public class BudgetResponseDto {
   // private Long id;
    private String name;
    private LocalDate startDate;
    private LocalDate endDate;
    private String periodType;
    private Double totalAmount;
    private boolean active;
    private Long userId;
}