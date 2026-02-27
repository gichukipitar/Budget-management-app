package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
import com.sirhpitar.budget.dtos.request.LoginRequestDto;
import com.sirhpitar.budget.dtos.request.RegisterRequestDto;
import com.sirhpitar.budget.dtos.request.ResendVerificationRequestDto;
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
    public Mono<ResponseEntity<ApiResponse<Void>>> register(@Valid @RequestBody RegisterRequestDto dto) {
        return authService.register(dto)
                .map(v -> ApiResponseUtil.success("Registration successful. Verify your email.", null));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<AuthResponseDto>>> login(@Valid @RequestBody LoginRequestDto dto) {
        return authService.login(dto)
                .map(r -> {
                    ResponseEntity<ApiResponse<AuthResponseDto>> base =
                            ApiResponseUtil.success("Login successful", r.getBody());

                    return ResponseEntity
                            .status(base.getStatusCode())
                            .headers(base.getHeaders())
                            .header("Set-Cookie", r.getRefreshCookie().toString())
                            .body(base.getBody());
                });
    }

    @GetMapping("/verify-email")
    public Mono<ResponseEntity<ApiResponse<Void>>> verifyEmail(@RequestParam("token") String token) {
        return authService.verifyEmail(token)
                .map(v -> ApiResponseUtil.success("Email verified successfully", null));
    }

    @PostMapping("/resend-verification")
    public Mono<ResponseEntity<ApiResponse<Void>>> resendVerification(@Valid @RequestBody ResendVerificationRequestDto dto) {
        return authService.resendVerification(dto.getEmail())
                .map(v -> ApiResponseUtil.success("Verification email resent", null));
    }

    @PostMapping("/refresh")
    public Mono<ResponseEntity<ApiResponse<AuthResponseDto>>> refresh(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        return authService.refresh(refreshToken)
                .map(r -> {
                    ResponseEntity<ApiResponse<AuthResponseDto>> base =
                            ApiResponseUtil.success("Token refreshed", r.getBody());

                    return ResponseEntity
                            .status(base.getStatusCode())
                            .headers(base.getHeaders())
                            .header("Set-Cookie", r.getRefreshCookie().toString())
                            .body(base.getBody());
                });
    }

    @PostMapping("/logout")
    public Mono<ResponseEntity<ApiResponse<Void>>> logout(
            @CookieValue(name = "refreshToken", required = false) String refreshToken
    ) {
        return authService.logout(refreshToken)
                .thenReturn(
                        ResponseEntity.ok()
                                .header("Set-Cookie", "refreshToken=; Path=/api/auth; Max-Age=0; HttpOnly; SameSite=Lax")
                                .body(ApiResponseUtil.successVoid("Logged out").getBody())
                );
    }
}