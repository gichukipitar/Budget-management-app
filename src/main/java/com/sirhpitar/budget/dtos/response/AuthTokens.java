package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthTokens {
    private AuthResponseDto access;
    private String refreshToken;
}
