package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeResponseDto {
    private Long id;
    private String username;
    private String email;
}
