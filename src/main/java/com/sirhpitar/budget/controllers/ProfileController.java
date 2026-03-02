package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
import com.sirhpitar.budget.dtos.request.ChangeEmailRequestDto;
import com.sirhpitar.budget.dtos.request.ChangePasswordRequestDto;
import com.sirhpitar.budget.dtos.request.DeleteAccountRequestDto;
import com.sirhpitar.budget.dtos.request.UpdateProfileRequestDto;
import com.sirhpitar.budget.dtos.response.MeResponseDto;
import com.sirhpitar.budget.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    private String email(Jwt jwt) {
        return jwt.getClaimAsString("email");
    }

    @GetMapping("/me")
    public Mono<ResponseEntity<ApiResponse<MeResponseDto>>> me(@AuthenticationPrincipal Jwt jwt) {
        return profileService.me(email(jwt))
                .map(data -> ApiResponseUtil.success("Profile fetched successfully", data));
    }

    @PatchMapping
    public Mono<ResponseEntity<ApiResponse<MeResponseDto>>> updateProfile(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UpdateProfileRequestDto dto
    ) {
        return profileService.updateProfile(email(jwt), dto)
                .map(data -> ApiResponseUtil.success("Profile updated successfully", data));
    }

    @PostMapping("/change-password")
    public Mono<ResponseEntity<ApiResponse<Void>>> changePassword(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangePasswordRequestDto dto
    ) {
        return profileService.changePassword(email(jwt), dto)
                .thenReturn(ApiResponseUtil.successVoid("Password changed successfully"));
    }


    @PostMapping("/change-email")
    public Mono<ResponseEntity<ApiResponse<Void>>> requestEmailChange(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChangeEmailRequestDto dto
    ) {
        return profileService.requestEmailChange(email(jwt), dto)
                .thenReturn(ApiResponseUtil.successVoid("Email change requested. Check your new email to verify."));
    }

    @GetMapping("/verify-email-change")
    public Mono<ResponseEntity<ApiResponse<Void>>> verifyEmailChange(
            @RequestParam("token") String token
    ) {
        return profileService.verifyEmailChange(token)
                .thenReturn(ApiResponseUtil.successVoid("New email verified successfully"));
    }


    @PostMapping(value = "/picture", consumes = "multipart/form-data")
    public Mono<ResponseEntity<ApiResponse<MeResponseDto>>> uploadPicture(
            @AuthenticationPrincipal Jwt jwt,
            @RequestPart("file") FilePart file
    ) {
        return profileService.uploadProfilePicture(email(jwt), file)
                .map(data -> ApiResponseUtil.success("Profile picture updated successfully", data));
    }


    @DeleteMapping
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteAccount(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody DeleteAccountRequestDto dto
    ) {
        return profileService.deleteAccount(email(jwt), dto)
                .thenReturn(ApiResponseUtil.successVoid("Account deleted successfully"));
    }
}