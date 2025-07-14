package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@Builder
public class FinancialGoalResponseDto {
    private Long id;
    private String name;
    private Double targetAmount;
    private Double currentAmount;
    private LocalDate targetDate;
    private String priority;
    private String category;
    private String description;
    private boolean achieved;
    private Long userId;
}
