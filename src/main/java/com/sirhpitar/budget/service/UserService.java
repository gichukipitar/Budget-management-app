package com.sirhpitar.budget.service;

import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.UserResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface UserService {
    Mono<UserResponseDto> createUser(UserRequestDto dto);

    Mono<UserResponseDto> getUserById(Long id);

    Flux<UserResponseDto> getAllUsers();

    Mono<UserResponseDto> updateUser(Long id, UserRequestDto dto);

    Mono<Void> deleteUser(Long id);
}
