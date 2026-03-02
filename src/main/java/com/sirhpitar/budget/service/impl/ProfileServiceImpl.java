package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.dtos.request.ChangePasswordRequestDto;
import com.sirhpitar.budget.dtos.request.UpdateProfileRequestDto;
import com.sirhpitar.budget.dtos.response.MeResponseDto;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.exceptions.NotFoundException;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.ProfileService;
import com.sirhpitar.budget.utils.ReactorBlocking;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<MeResponseDto> me(String email) {
        return ReactorBlocking.mono(() -> toMe(findByEmail(email)));
    }

    @Override
    public Mono<MeResponseDto> updateProfile(String email, UpdateProfileRequestDto dto) {
        return ReactorBlocking.mono(() -> {
            User user = findByEmail(email);

            if (dto.getFirstName() != null) {
                String v = dto.getFirstName().trim();
                if (v.isBlank()) throw new IllegalArgumentException("First name cannot be blank");
                user.setFirstName(v);
            }

            if (dto.getLastName() != null) {
                String v = dto.getLastName().trim();
                if (v.isBlank()) throw new IllegalArgumentException("Last name cannot be blank");
                user.setLastName(v);
            }

            if (dto.getCurrency() != null) {
                user.setCurrency(dto.getCurrency().trim().toUpperCase());
            }

            if (dto.getTimezone() != null) {
                String v = dto.getTimezone().trim();
                if (v.isBlank()) throw new IllegalArgumentException("Timezone cannot be blank");
                user.setTimezone(v);
            }

            if (dto.getProfilePictureUrl() != null) {
                String v = dto.getProfilePictureUrl().trim();
                if (v.isBlank()) user.setProfilePictureUrl(null);
                else user.setProfilePictureUrl(v);
            }

            User saved = userRepository.save(user);
            return toMe(saved);
        });
    }

    @Override
    public Mono<Void> changePassword(String email, ChangePasswordRequestDto dto) {
        return ReactorBlocking.run(() -> {
            User user = findByEmail(email);

            if (!passwordEncoder.matches(dto.getOldPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Old password is incorrect");
            }

            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            userRepository.save(user);
        });
    }

    private User findByEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private MeResponseDto toMe(User user) {
        return new MeResponseDto(user.getId(), user.getUsername(), user.getEmail());
    }
}