package com.sirhpitar.budget.service;

import com.sirhpitar.budget.dtos.request.LoginRequestDto;
import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.AuthResponseDto;
import reactor.core.publisher.Mono;

public interface AuthService {
    Mono<AuthResponseDto> register(UserRequestDto dto);
    Mono<AuthResponseDto> login(LoginRequestDto dto);
}
