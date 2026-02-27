package com.sirhpitar.budget.service;

import com.sirhpitar.budget.dtos.request.LoginRequestDto;
import com.sirhpitar.budget.dtos.request.RegisterRequestDto;
import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.AuthCookieResponse;
import com.sirhpitar.budget.dtos.response.AuthResponseDto;
import com.sirhpitar.budget.dtos.response.AuthTokens;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<Void> register(RegisterRequestDto dto);


    Mono<AuthCookieResponse> login(LoginRequestDto dto);


    Mono<AuthCookieResponse> refresh(String refreshToken);

    Mono<Void> logout(String refreshToken);

    Mono<Void> verifyEmail(String token);
    Mono<Void> resendVerification(String email);
}