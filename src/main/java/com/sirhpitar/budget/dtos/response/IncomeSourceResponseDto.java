package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Setter
@Builder
@AllArgsConstructor
@Getter
public class IncomeSourceResponseDto {
    private Long id;
    private String name;
    private String type;
    private String category;
    private Double amount;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;
    private String description;
    private Long userId;
}
