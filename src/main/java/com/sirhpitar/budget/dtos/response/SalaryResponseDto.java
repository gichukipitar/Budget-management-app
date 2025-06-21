package com.sirhpitar.budget.dtos.response;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
@Setter
@Builder
@NoArgsConstructor
public class SalaryResponseDto {
    private Long id;
    private Long userId;
    private Double amount;
    private LocalDate dateReceived;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
