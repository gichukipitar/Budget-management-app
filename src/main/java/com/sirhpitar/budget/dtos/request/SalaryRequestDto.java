package com.sirhpitar.budget.dtos.request;

import lombok.*;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
@Setter
public class SalaryRequestDto {
    private Long userId;
    private Double amount;
    private LocalDate dateReceived;
    private String description;
}
