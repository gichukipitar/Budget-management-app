package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
import com.sirhpitar.budget.dtos.request.ChangePasswordRequestDto;
import com.sirhpitar.budget.dtos.response.MeResponseDto;
import com.sirhpitar.budget.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse<MeResponseDto>>> me() {
        return profileService.me()
                .map(data -> ApiResponseUtil.success("Profile fetched successfully", data));
    }

    @PostMapping("/change-password")
    public Mono<ResponseEntity<ApiResponse<Void>>> changePassword(@Valid @RequestBody ChangePasswordRequestDto dto) {
        return profileService.changePassword(dto)
                .thenReturn(ApiResponseUtil.success("Password changed successfully", null));
    }
}
