package com.sirhpitar.budget.service;

import com.sirhpitar.budget.dtos.request.ChangeEmailRequestDto;
import com.sirhpitar.budget.dtos.request.ChangePasswordRequestDto;
import com.sirhpitar.budget.dtos.request.DeleteAccountRequestDto;
import com.sirhpitar.budget.dtos.request.UpdateProfileRequestDto;
import com.sirhpitar.budget.dtos.response.MeResponseDto;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

public interface ProfileService {

    Mono<MeResponseDto> me(String email);

    Mono<MeResponseDto> updateProfile(String email, UpdateProfileRequestDto dto);

    Mono<Void> changePassword(String email, ChangePasswordRequestDto dto);

    Mono<MeResponseDto> uploadProfilePicture(String email, FilePart file);

    Mono<Void> deleteAccount(String email, DeleteAccountRequestDto dto);

    Mono<Void> requestEmailChange(String email, ChangeEmailRequestDto dto);

    Mono<Void> verifyEmailChange(String token);
}