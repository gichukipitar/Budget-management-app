package com.sirhpitar.budget.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@AllArgsConstructor
@Getter
@Setter
@Builder
public class FinancialGoalRequestDto {
    private String name;
    private Double targetAmount;
    private Double currentAmount;
    private LocalDate targetDate;
    private String priority;
    private String category;
    private String description;
    private boolean achieved;
}
