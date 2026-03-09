package com.sirhpitar.budget.service;

import com.sirhpitar.budget.dtos.request.LoginRequestDto;
import com.sirhpitar.budget.dtos.request.RegisterRequestDto;
import com.sirhpitar.budget.dtos.response.AuthCookieResponse;
import com.sirhpitar.budget.dtos.response.Setup2faResponseDto;
import reactor.core.publisher.Mono;

public interface AuthService {

    Mono<Void> register(RegisterRequestDto dto);

    Mono<AuthCookieResponse> login(LoginRequestDto dto);

    Mono<AuthCookieResponse> refresh(String refreshToken);

    Mono<Void> logout(String refreshToken);

    Mono<Void> verifyEmail(String token);

    Mono<Void> resendVerification(String email);

    Mono<Void> forgotPassword(String email);

    Mono<Void> resetPassword(String token, String newPassword);

    Mono<Setup2faResponseDto> setup2fa(Long userId);

    Mono<Void> confirm2fa(Long userId, String code);

    Mono<AuthCookieResponse> verifyLogin2fa(String loginChallengeToken, String code);

    Mono<Void> disable2fa(Long userId, String password, String code);
}