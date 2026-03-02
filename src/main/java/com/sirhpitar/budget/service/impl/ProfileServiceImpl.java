package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.config.AuthProps;
import com.sirhpitar.budget.dtos.request.*;
import com.sirhpitar.budget.dtos.response.MeResponseDto;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.exceptions.NotFoundException;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.AccountEmailService;
import com.sirhpitar.budget.service.EmailVerificationService;
import com.sirhpitar.budget.service.ProfileService;
import com.sirhpitar.budget.utils.ReactorBlocking;
import lombok.RequiredArgsConstructor;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // NEW deps
    private final AuthProps authProps;
    private final EmailVerificationService emailVerificationService;
    private final AccountEmailService accountEmailService;

    private final Path uploadRoot = Paths.get("uploads");

    @Override
    public Mono<MeResponseDto> me(String email) {
        return ReactorBlocking.mono(() -> toMe(findByEmail(email)));
    }

    @Override
    public Mono<MeResponseDto> updateProfile(String email, UpdateProfileRequestDto dto) {
        return ReactorBlocking.mono(() -> {
            User user = findByEmail(email);

            boolean changed = false;
            StringBuilder changes = new StringBuilder();

            if (dto.getFirstName() != null) {
                String v = dto.getFirstName().trim();
                if (v.isBlank()) throw new IllegalArgumentException("First name cannot be blank");
                user.setFirstName(v);
                changed = true;
                changes.append("firstName, ");
            }

            if (dto.getLastName() != null) {
                String v = dto.getLastName().trim();
                if (v.isBlank()) throw new IllegalArgumentException("Last name cannot be blank");
                user.setLastName(v);
                changed = true;
                changes.append("lastName, ");
            }

            if (dto.getCurrency() != null) {
                user.setCurrency(dto.getCurrency().trim().toUpperCase());
                changed = true;
                changes.append("currency, ");
            }

            if (dto.getTimezone() != null) {
                String v = dto.getTimezone().trim();
                if (v.isBlank()) throw new IllegalArgumentException("Timezone cannot be blank");
                user.setTimezone(v);
                changed = true;
                changes.append("timezone, ");
            }

            // keep this if you still want manual URL updates:
            if (dto.getProfilePictureUrl() != null) {
                String v = dto.getProfilePictureUrl().trim();
                if (v.isBlank()) user.setProfilePictureUrl(null);
                else user.setProfilePictureUrl(v);
                changed = true;
                changes.append("profilePictureUrl, ");
            }

            User saved = userRepository.save(user);

            if (changed) {
                accountEmailService.sendProfileChangedEmail(saved.getEmail(),
                        "Updated fields: " + (changes.length() > 2 ? changes.substring(0, changes.length() - 2) : "profile"));
            }

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

            // optional safety: prevent same password
            if (passwordEncoder.matches(dto.getNewPassword(), user.getPassword())) {
                throw new IllegalArgumentException("New password must be different");
            }

            user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            userRepository.save(user);

            accountEmailService.sendPasswordChangedEmail(user.getEmail());
        });
    }

    @Override
    public Mono<Void> changeEmail(String email, ChangeEmailRequestDto dto) {
        return ReactorBlocking.run(() -> {
            User user = findByEmail(email);
            String newEmail = dto.getNewEmail().toLowerCase().trim();

            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Invalid password");
            }

            // prevent duplicates
            userRepository.findByEmail(newEmail).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new IllegalArgumentException("Email already in use");
                }
            });

            String oldEmail = user.getEmail();

            // notify old email first (security)
            accountEmailService.sendEmailChangeRequestedOldEmail(oldEmail, newEmail);

            // switch email + force re-verification
            user.setEmail(newEmail);
            user.setEmailVerified(false);
            user.setEnabled(false);

            String token = UUID.randomUUID().toString();
            user.setEmailVerificationToken(token);
            user.setEmailVerificationTokenExpiry(
                    Instant.now().plus(authProps.verificationTokenMinutes(), ChronoUnit.MINUTES)
            );
            user.setEmailVerificationSentAt(Instant.now());

            User saved = userRepository.save(user);

            // send verification to NEW email
            emailVerificationService.sendVerificationEmail(saved, token);
        });
    }

    @Override
    public Mono<MeResponseDto> uploadProfilePicture(String email, FilePart file) {
        return ReactorBlocking.mono(() -> {
            if (file == null) throw new IllegalArgumentException("File is required");

            User user = findByEmail(email);

            String fn = file.filename().toLowerCase();
            if (!(fn.endsWith(".png") || fn.endsWith(".jpg") || fn.endsWith(".jpeg") || fn.endsWith(".webp"))) {
                throw new IllegalArgumentException("Only png/jpg/jpeg/webp allowed");
            }

            Files.createDirectories(uploadRoot);

            String storedName = "user-" + user.getId() + "-" + System.currentTimeMillis() + "-" + file.filename();
            Path dest = uploadRoot.resolve(storedName);

            // block because you're using blocking repos
            file.transferTo(dest).block();

            // You must serve /uploads via static mapping (later). For now store the path:
            String url = "/uploads/" + storedName;
            user.setProfilePictureUrl(url);

            User saved = userRepository.save(user);

            accountEmailService.sendProfileChangedEmail(saved.getEmail(), "Profile picture updated");

            return toMe(saved);
        });
    }

    @Override
    public Mono<Void> deleteAccount(String email, DeleteAccountRequestDto dto) {
        return ReactorBlocking.run(() -> {
            User user = findByEmail(email);

            if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Invalid password");
            }

            if (dto.getConfirm() != null && !dto.getConfirm().isBlank()) {
                if (!"DELETE".equalsIgnoreCase(dto.getConfirm().trim())) {
                    throw new IllegalArgumentException("Confirm must be DELETE");
                }
            }

            String userEmail = user.getEmail();

            userRepository.delete(user);

            accountEmailService.sendAccountDeletedEmail(userEmail);
        });
    }

    // -------------------- helpers --------------------

    private User findByEmail(String email) {
        if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");
        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private MeResponseDto toMe(User user) {
        // keep your MeResponseDto minimal, OR expand it if you want
        return new MeResponseDto(user.getId(), user.getUsername(), user.getEmail());
    }
}