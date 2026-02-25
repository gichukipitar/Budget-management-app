package com.sirhpitar.budget.service;

import com.sirhpitar.budget.dtos.request.ChangePasswordRequestDto;
import com.sirhpitar.budget.dtos.response.MeResponseDto;
import reactor.core.publisher.Mono;

public interface ProfileService {
    Mono<MeResponseDto> me(String email);
    Mono<Void> changePassword(String email, ChangePasswordRequestDto dto);
}