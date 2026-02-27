package com.sirhpitar.budget.dtos.response;

import lombok.*;
import org.springframework.http.ResponseCookie;

@Data
@AllArgsConstructor
public class AuthCookieResponse {
    private AuthResponseDto body;
    private ResponseCookie refreshCookie;
}
