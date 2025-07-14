package com.sirhpitar.budget.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Builder
@AllArgsConstructor
@Getter
public class IncomeSourceRequestDto {
    private String name;
    private String type; // Salary, Bonus, Freelance, etc.
    private String category;
    private Double amount;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private String description;
}
