package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
import com.sirhpitar.budget.dtos.request.ChangePasswordRequestDto;
import com.sirhpitar.budget.dtos.response.MeResponseDto;
import com.sirhpitar.budget.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse<MeResponseDto>>> me(@AuthenticationPrincipal Jwt jwt) {
        String email = jwt.getClaimAsString("email");
        return profileService.me(email)
                .map(data -> ApiResponseUtil.success("Profile fetched successfully", data));
    }

    @PostMapping("/change-password")
    public Mono<ResponseEntity<ApiResponse<Void>>> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequestDto dto
    ) {
        String email = jwt.getClaimAsString("email");
        return profileService.changePassword(email, dto)
                .thenReturn(ApiResponseUtil.success("Password changed successfully", null));
    }
}