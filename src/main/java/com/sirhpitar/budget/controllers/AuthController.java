package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
import com.sirhpitar.budget.dtos.request.LoginRequestDto;
import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.AuthResponseDto;
import com.sirhpitar.budget.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public Mono<ResponseEntity<ApiResponse<AuthResponseDto>>> register(@Valid @RequestBody UserRequestDto dto) {
        return authService.register(dto)
                .map(data -> ApiResponseUtil.success("Registration successful", data));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<AuthResponseDto>>> login(@Valid @RequestBody LoginRequestDto dto) {
        return authService.login(dto)
                .map(data -> ApiResponseUtil.success("Login successful", data));
    }
}
