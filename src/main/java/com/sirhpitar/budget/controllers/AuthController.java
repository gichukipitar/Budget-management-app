package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
import com.sirhpitar.budget.dtos.request.*;
import com.sirhpitar.budget.dtos.response.AuthResponseDto;
import com.sirhpitar.budget.dtos.response.Setup2faResponseDto;
import com.sirhpitar.budget.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
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
                .thenReturn(ApiResponseUtil.successVoid("Registration successful. Verify your email."));
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<ApiResponse<AuthResponseDto>>> login(@Valid @RequestBody LoginRequestDto dto) {
        return authService.login(dto)
                .map(r -> {
                    ResponseEntity<ApiResponse<AuthResponseDto>> base =
                            ApiResponseUtil.success("Login successful", r.getBody());

                    // if no refresh cookie (MFA required), just return base
                    if (r.getRefreshCookie() == null) {
                        return ResponseEntity
                                .status(base.getStatusCode())
                                .headers(base.getHeaders())
                                .body(base.getBody());
                    }

                    return ResponseEntity
                            .status(base.getStatusCode())
                            .headers(base.getHeaders())
                            .header("Set-Cookie", r.getRefreshCookie().toString())
                            .body(base.getBody());
                });
    }

    @PostMapping("/2fa/verify-login")
    public Mono<ResponseEntity<ApiResponse<AuthResponseDto>>> verifyLogin2fa(@Valid @RequestBody VerifyLogin2faRequestDto dto) {
        return authService.verifyLogin2fa(dto.getLoginChallengeToken(), dto.getCode())
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

    @PostMapping("/2fa/setup")
    public Mono<ResponseEntity<ApiResponse<Setup2faResponseDto>>> setup2fa(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.parseLong(jwt.getSubject());
        return authService.setup2fa(userId)
                .map(r -> ApiResponseUtil.success("2FA setup generated", r));
    }

    @PostMapping("/2fa/confirm")
    public Mono<ResponseEntity<ApiResponse<Void>>> confirm2fa(@AuthenticationPrincipal Jwt jwt,
                                                              @Valid @RequestBody Confirm2faRequestDto dto) {
        Long userId = Long.parseLong(jwt.getSubject());
        return authService.confirm2fa(userId, dto.getCode())
                .thenReturn(ApiResponseUtil.successVoid("2FA enabled successfully"));
    }

    @PostMapping("/2fa/disable")
    public Mono<ResponseEntity<ApiResponse<Void>>> disable2fa(@AuthenticationPrincipal Jwt jwt,
                                                              @Valid @RequestBody Disable2faRequestDto dto) {
        Long userId = Long.parseLong(jwt.getSubject());
        return authService.disable2fa(userId, dto.getPassword(), dto.getCode())
                .thenReturn(ApiResponseUtil.successVoid("2FA disabled successfully"));
    }

    @GetMapping("/verify-email")
    public Mono<ResponseEntity<ApiResponse<Void>>> verifyEmail(@RequestParam("token") String token) {
        return authService.verifyEmail(token)
                .thenReturn(ApiResponseUtil.successVoid("Email verified successfully"));
    }

    @PostMapping("/resend-verification")
    public Mono<ResponseEntity<ApiResponse<Void>>> resendVerification(@Valid @RequestBody EmailRequestDto dto) {
        return authService.resendVerification(dto.getEmail())
                .thenReturn(ApiResponseUtil.successVoid("Verification email resent"));
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

    @PostMapping("/forgot-password")
    public Mono<ResponseEntity<ApiResponse<Void>>> forgotPassword(@Valid @RequestBody EmailRequestDto dto) {
        return authService.forgotPassword(dto.getEmail())
                .thenReturn(ApiResponseUtil.successVoid("Password reset email sent"));
    }

    @PostMapping("/reset-password")
    public Mono<ResponseEntity<ApiResponse<Void>>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto dto) {
        return authService.resetPassword(dto.getToken(), dto.getNewPassword())
                .thenReturn(ApiResponseUtil.successVoid("Password reset successful"));
    }
}