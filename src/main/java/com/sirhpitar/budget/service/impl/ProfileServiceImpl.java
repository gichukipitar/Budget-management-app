package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.config.AuthProps;
import com.sirhpitar.budget.dtos.request.ChangeEmailRequestDto;
import com.sirhpitar.budget.dtos.request.ChangePasswordRequestDto;
import com.sirhpitar.budget.dtos.request.DeleteAccountRequestDto;
import com.sirhpitar.budget.dtos.request.UpdateProfileRequestDto;
import com.sirhpitar.budget.dtos.response.MeResponseDto;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.exceptions.BadRequestException;
import com.sirhpitar.budget.exceptions.NotFoundException;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.AccountNotificationEmailService;
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
    private final AuthProps authProps;
    private final EmailVerificationService emailVerificationService;
    private final AccountNotificationEmailService accountEmailService;

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
                String trimmed = dto.getFirstName().trim();
                if (trimmed.isBlank()) {
                    throw new BadRequestException("First name cannot be blank");
                }
                user.setFirstName(trimmed);
                changed = true;
                changes.append("firstName, ");
            }

            if (dto.getLastName() != null) {
                String trimmed = dto.getLastName().trim();
                if (trimmed.isBlank()) {
                    throw new BadRequestException("Last name cannot be blank");
                }
                user.setLastName(trimmed);
                changed = true;
                changes.append("lastName, ");
            }

            if (dto.getCurrency() != null) {
                String trimmed = dto.getCurrency().trim();
                user.setCurrency(trimmed.isBlank() ? null : trimmed.toUpperCase());
                changed = true;
                changes.append("currency, ");
            }

            if (dto.getTimezone() != null) {
                String trimmed = dto.getTimezone().trim();
                if (trimmed.isBlank()) {
                    throw new BadRequestException("Timezone cannot be blank");
                }
                user.setTimezone(trimmed);
                changed = true;
                changes.append("timezone, ");
            }

            if (dto.getProfilePictureUrl() != null) {
                String trimmed = dto.getProfilePictureUrl().trim();
                user.setProfilePictureUrl(trimmed.isBlank() ? null : trimmed);
                changed = true;
                changes.append("profilePictureUrl, ");
            }

            User saved = userRepository.save(user);

            if (changed) {
                String updated = changes.length() > 2
                        ? changes.substring(0, changes.length() - 2)
                        : "profile";

                accountEmailService
                        .sendProfileChangedEmail(saved.getEmail(), "Updated fields: " + updated)
                        .block();
            }

            return toMe(saved);
        });
    }

    @Override
    public Mono<Void> changePassword(String email, ChangePasswordRequestDto dto) {
        return ReactorBlocking.run(() -> {
            User user = findByEmail(email);

            if (!passwordEncoder.matches(dto.getOldPassword(), user.getPasswordHash())) {
                throw new BadRequestException("Old password is incorrect");
            }

            if (passwordEncoder.matches(dto.getNewPassword(), user.getPasswordHash())) {
                throw new BadRequestException("New password must be different");
            }

            user.setPasswordHash(passwordEncoder.encode(dto.getNewPassword()));
            userRepository.save(user);

            accountEmailService
                    .sendPasswordChangedEmail(user.getEmail())
                    .block();
        });
    }

    @Override
    public Mono<Void> requestEmailChange(String email, ChangeEmailRequestDto dto) {
        return ReactorBlocking.run(() -> {
            User user = findByEmail(email);

            String newEmail = dto.getNewEmail();
            if (newEmail == null || newEmail.isBlank()) {
                throw new BadRequestException("New email is required");
            }
            newEmail = newEmail.toLowerCase().trim();

            if (newEmail.equalsIgnoreCase(user.getEmail())) {
                throw new BadRequestException("New email must be different from the current email");
            }

            if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
                throw new BadRequestException("Invalid password");
            }

            userRepository.findByEmail(newEmail).ifPresent(existing -> {
                if (!existing.getId().equals(user.getId())) {
                    throw new BadRequestException("Email already in use");
                }
            });

            String oldEmail = user.getEmail();

            accountEmailService
                    .sendEmailChangeRequestedOldEmail(oldEmail, newEmail)
                    .block();

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

            emailVerificationService
                    .sendVerificationEmail(saved.getEmail(), saved.getFirstName(), token)
                    .block();
        });
    }

    @Override
    public Mono<Void> verifyEmailChange(String token) {
        return ReactorBlocking.run(() -> {
            if (token == null || token.isBlank()) {
                throw new BadRequestException("Verification token is required");
            }

            User user = userRepository.findByEmailVerificationToken(token.trim())
                    .orElseThrow(() -> new NotFoundException("Invalid or expired verification token"));

            Instant expiry = user.getEmailVerificationTokenExpiry();
            if (expiry == null || expiry.isBefore(Instant.now())) {
                throw new BadRequestException("Verification token expired");
            }

            user.setEmailVerified(true);
            user.setEnabled(true);
            user.setEmailVerificationToken(null);
            user.setEmailVerificationTokenExpiry(null);
            user.setEmailVerificationSentAt(null);

            userRepository.save(user);

            accountEmailService
                    .sendProfileChangedEmail(
                            user.getEmail(),
                            "Your email address was verified successfully."
                    )
                    .block();
        });
    }

    @Override
    public Mono<MeResponseDto> uploadProfilePicture(String email, FilePart file) {
        return ReactorBlocking.mono(() -> {
            if (file == null) {
                throw new BadRequestException("File is required");
            }

            User user = findByEmail(email);

            String filename = file.filename();
            if (filename == null || filename.isBlank()) {
                throw new BadRequestException("Invalid file name");
            }

            String lower = filename.toLowerCase();
            if (!(lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".webp"))) {
                throw new BadRequestException("Only png, jpg, jpeg, and webp files are allowed");
            }

            Files.createDirectories(uploadRoot);

            String storedName = "user-" + user.getId() + "-" + System.currentTimeMillis() + "-" + filename;
            Path dest = uploadRoot.resolve(storedName);

            file.transferTo(dest).block();

            String url = "/uploads/" + storedName;
            user.setProfilePictureUrl(url);

            User saved = userRepository.save(user);

            accountEmailService
                    .sendProfileChangedEmail(saved.getEmail(), "Profile picture updated")
                    .block();

            return toMe(saved);
        });
    }

    @Override
    public Mono<Void> deleteAccount(String email, DeleteAccountRequestDto dto) {
        return ReactorBlocking.run(() -> {
            User user = findByEmail(email);

            if (!passwordEncoder.matches(dto.getPassword(), user.getPasswordHash())) {
                throw new BadRequestException("Invalid password");
            }

            if (dto.getConfirm() != null && !dto.getConfirm().isBlank()) {
                if (!"DELETE".equalsIgnoreCase(dto.getConfirm().trim())) {
                    throw new BadRequestException("Confirm must be DELETE");
                }
            }

            String userEmail = user.getEmail();
            userRepository.delete(user);

            accountEmailService
                    .sendAccountDeletedEmail(userEmail)
                    .block();
        });
    }

    private User findByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        return userRepository.findByEmail(email.toLowerCase().trim())
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private MeResponseDto toMe(User user) {
        return new MeResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}