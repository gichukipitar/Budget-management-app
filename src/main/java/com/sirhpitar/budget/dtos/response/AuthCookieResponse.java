package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.http.ResponseCookie;

@Data
@AllArgsConstructor
public class AuthCookieResponse {
    private AuthResponseDto body;
    private ResponseCookie refreshCookie;
}
