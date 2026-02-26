package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.config.AuthProps;
import com.sirhpitar.budget.config.JwtProps;
import com.sirhpitar.budget.dtos.request.LoginRequestDto;
import com.sirhpitar.budget.dtos.request.RegisterRequestDto;
import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.AuthResponseDto;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.exceptions.NotFoundException;
import com.sirhpitar.budget.exceptions.TooManyRequestsException;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.AuthService;
import com.sirhpitar.budget.service.EmailVerificationService;
import com.sirhpitar.budget.utils.ReactorBlocking;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProps jwtProps;
    private final AuthProps authProps;
    private final EmailVerificationService emailVerificationService;

    @Override
    public Mono<Void> register(RegisterRequestDto dto) {
        return ReactorBlocking.run(() -> {
            String email = dto.getEmail().toLowerCase().trim();
            String username = dto.getUsername().trim();

            userRepository.findByEmail(email).ifPresent(u -> {
                throw new IllegalArgumentException("Email already in use");
            });
            userRepository.findByUsername(username).ifPresent(u -> {
                throw new IllegalArgumentException("Username already in use");
            });

            if (!dto.isTermsAccepted()) {
                throw new IllegalArgumentException("Terms of service must be accepted");
            }

            User user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setFirstName(dto.getFirstName().trim());
            user.setLastName(dto.getLastName().trim());
            user.setTermsAccepted(dto.isTermsAccepted());

            user.setEnabled(false);
            user.setEmailVerified(false);
            String token = UUID.randomUUID().toString();
            user.setEmailVerificationToken(token);
            user.setEmailVerificationTokenExpiry(Instant.now().plus(authProps.verificationTokenMinutes(), ChronoUnit.MINUTES));
            user.setEmailVerificationSentAt(Instant.now());

            User saved = userRepository.save(user);
            emailVerificationService.sendVerificationEmail(saved, token);
        });
    }

    @Override
    public Mono<AuthResponseDto> login(LoginRequestDto dto) {
        return ReactorBlocking.mono(() -> {
            String identifier = dto.getIdentifier().trim();

            User user = userRepository.findByEmail(identifier.toLowerCase())
                    .orElseGet(() -> userRepository.findByUsername(identifier)
                            .orElseThrow(() -> new NotFoundException("Invalid credentials")));

            if (!user.isEmailVerified()) throw new IllegalArgumentException("Email not verified");
            if (!user.isEnabled()) throw new IllegalArgumentException("Account disabled");

            if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
                throw new IllegalArgumentException("Account locked. Try again later.");
            }

            boolean ok = passwordEncoder.matches(dto.getPassword(), user.getPassword());
            if (!ok) {
                int fails = user.getFailedLoginAttempts() + 1;
                user.setFailedLoginAttempts(fails);

                if (fails >= authProps.maxFailedAttempts()) {
                    user.setLockedUntil(Instant.now().plus(authProps.lockMinutes(), ChronoUnit.MINUTES));
                    user.setFailedLoginAttempts(0);
                }

                userRepository.save(user);
                throw new NotFoundException("Invalid credentials");
            }

            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);
            userRepository.save(user);

            return new AuthResponseDto(issueToken(user), "Bearer");
        });
    }

    @Override
    public Mono<Void> verifyEmail(String token) {
        return ReactorBlocking.run(() -> {
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Verification token is required");
            }

            User user = userRepository.findByEmailVerificationToken(token.trim())
                    .orElseThrow(() -> new NotFoundException("Invalid or expired verification token"));

            Instant expiry = user.getEmailVerificationTokenExpiry();
            if (expiry == null || expiry.isBefore(Instant.now())) {
                throw new IllegalArgumentException("Verification token expired");
            }

            user.setEmailVerified(true);
            user.setEnabled(true);
            user.setEmailVerificationToken(null);
            user.setEmailVerificationTokenExpiry(null);
            user.setEmailVerificationSentAt(null);
            userRepository.save(user);
        });
    }

    @Override
    public Mono<Void> resendVerification(String email) {
        return ReactorBlocking.run(() -> {
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email is required");
            }

            User user = userRepository.findByEmail(email.toLowerCase().trim())
                    .orElseThrow(() -> new NotFoundException("User not found"));

            if (user.isEmailVerified()) {
                throw new IllegalArgumentException("Email already verified");
            }

            if (user.getEmailVerificationSentAt() != null && authProps.resendCooldownMinutes() > 0) {
                Instant nextAllowed = user.getEmailVerificationSentAt()
                        .plus(authProps.resendCooldownMinutes(), ChronoUnit.MINUTES);
                if (nextAllowed.isAfter(Instant.now())) {
                    throw new TooManyRequestsException("Verification email recently sent. Please try again later.");
                }
            }

            String token = UUID.randomUUID().toString();
            user.setEmailVerificationToken(token);
            user.setEmailVerificationTokenExpiry(Instant.now().plus(authProps.verificationTokenMinutes(), ChronoUnit.MINUTES));
            user.setEmailVerificationSentAt(Instant.now());
            userRepository.save(user);

            emailVerificationService.sendVerificationEmail(user, token);
        });
    }

    private String issueToken(User user) {

        Instant now = Instant.now();
        Instant expiry = now.plus(jwtProps.accessTokenMinutes(), ChronoUnit.MINUTES);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(jwtProps.issuer())
                .subject(user.getId().toString())
                .issuedAt(now)
                .expiresAt(expiry)
                .claim("email", user.getEmail())
                .claim("username", user.getUsername())
                .claim("role", "USER")
                .build();

        JwsHeader headers = JwsHeader.with(MacAlgorithm.HS256).build();

        return jwtEncoder
                .encode(JwtEncoderParameters.from(headers, claims))
                .getTokenValue();
    }


}
