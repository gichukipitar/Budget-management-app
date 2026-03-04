package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.config.AuthProps;
import com.sirhpitar.budget.config.JwtProps;
import com.sirhpitar.budget.dtos.request.LoginRequestDto;
import com.sirhpitar.budget.dtos.request.RegisterRequestDto;
import com.sirhpitar.budget.dtos.response.AuthCookieResponse;
import com.sirhpitar.budget.dtos.response.AuthResponseDto;
import com.sirhpitar.budget.dtos.response.Setup2faResponseDto;
import com.sirhpitar.budget.entities.RefreshToken;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.exceptions.NotFoundException;
import com.sirhpitar.budget.exceptions.TooManyRequestsException;
import com.sirhpitar.budget.repository.RefreshTokenRepository;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.AuthService;
import com.sirhpitar.budget.service.EmailVerificationService;
import com.sirhpitar.budget.utils.ReactorBlocking;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
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

            userRepository.findByEmail(email).ifPresent(u -> { throw new IllegalArgumentException("Email already in use"); });
            userRepository.findByUsername(username).ifPresent(u -> { throw new IllegalArgumentException("Username already in use"); });

            if (!dto.isTermsAccepted()) throw new IllegalArgumentException("Terms of service must be accepted");

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
            user.setEmailVerificationTokenExpiry(
                    Instant.now().plus(authProps.verificationTokenMinutes(), ChronoUnit.MINUTES)
            );
            user.setEmailVerificationSentAt(Instant.now());

            User saved = userRepository.save(user);

            // NOTE: this blocks; acceptable with your current ReactorBlocking + JPA approach
            emailVerificationService.sendVerificationEmail(saved, token).block();
        });
    }

    @Override
    public Mono<AuthCookieResponse> login(LoginRequestDto dto) {
        return ReactorBlocking.mono(() -> {
            User user = authenticate(dto);

            // TODO: When 2FA is implemented:
            // if (user.isMfaEnabled()) return AuthResponseDto(null, "Bearer", true, challengeToken) and no cookie.
            boolean mfaRequired = false;
            String loginChallengeToken = null;

            String accessToken = issueAccessToken(user);

            boolean rememberMe = dto.isRememberMe();
            int days = rememberMe ? authProps.refreshDaysRememberMe() : authProps.refreshDaysDefault();

            String refreshRaw = UUID.randomUUID().toString();
            saveRefreshToken(user.getId(), refreshRaw, Instant.now().plus(days, ChronoUnit.DAYS), rememberMe);

            return new AuthCookieResponse(
                    new AuthResponseDto(accessToken, "Bearer", mfaRequired, loginChallengeToken),
                    buildRefreshCookie(refreshRaw, days)
            );
        });
    }

    @Override
    public Mono<AuthCookieResponse> refresh(String refreshToken) {
        return ReactorBlocking.mono(() -> {
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new IllegalArgumentException("Refresh token is required");
            }

            String hash = sha256(refreshToken.trim());

            RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                    .orElseThrow(() -> new NotFoundException("Invalid refresh token"));

            if (existing.isRevoked()) throw new IllegalArgumentException("Refresh token revoked");
            if (existing.getExpiresAt() == null || existing.getExpiresAt().isBefore(Instant.now())) {
                throw new IllegalArgumentException("Refresh token expired");
            }

            User user = userRepository.findById(existing.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found"));

            // rotate
            existing.setRevoked(true);
            refreshTokenRepository.save(existing);

            boolean rememberMe = existing.isRememberMe();
            int days = rememberMe ? authProps.refreshDaysRememberMe() : authProps.refreshDaysDefault();

            String newRefresh = UUID.randomUUID().toString();
            saveRefreshToken(user.getId(), newRefresh, Instant.now().plus(days, ChronoUnit.DAYS), rememberMe);

            String newAccess = issueAccessToken(user);

            return new AuthCookieResponse(
                    new AuthResponseDto(newAccess, "Bearer", false, null),
                    buildRefreshCookie(newRefresh, days)
            );
        });
    }

    @Override
    public Mono<Void> logout(String refreshToken) {
        return ReactorBlocking.run(() -> {
            if (refreshToken == null || refreshToken.isBlank()) return;

            String hash = sha256(refreshToken.trim());
            refreshTokenRepository.findByTokenHash(hash).ifPresent(t -> {
                t.setRevoked(true);
                refreshTokenRepository.save(t);
            });
        });
    }

    @Override
    public Mono<Void> verifyEmail(String token) {
        return ReactorBlocking.run(() -> {
            if (token == null || token.isBlank()) throw new IllegalArgumentException("Verification token is required");

            User user = userRepository.findByEmailVerificationToken(token.trim())
                    .orElseThrow(() -> new NotFoundException("Invalid or expired verification token"));

            Instant expiry = user.getEmailVerificationTokenExpiry();
            if (expiry == null || expiry.isBefore(Instant.now())) throw new IllegalArgumentException("Verification token expired");

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
            if (email == null || email.isBlank()) throw new IllegalArgumentException("Email is required");

            User user = userRepository.findByEmail(email.toLowerCase().trim())
                    .orElseThrow(() -> new NotFoundException("User not found"));

            if (user.isEmailVerified()) throw new IllegalArgumentException("Email already verified");

            if (user.getEmailVerificationSentAt() != null && authProps.resendCooldownMinutes() > 0) {
                Instant nextAllowed = user.getEmailVerificationSentAt()
                        .plus(authProps.resendCooldownMinutes(), ChronoUnit.MINUTES);
                if (nextAllowed.isAfter(Instant.now())) {
                    throw new TooManyRequestsException("Verification email recently sent. Please try again later.");
                }
            }

            String token = UUID.randomUUID().toString();
            user.setEmailVerificationToken(token);
            user.setEmailVerificationTokenExpiry(
                    Instant.now().plus(authProps.verificationTokenMinutes(), ChronoUnit.MINUTES)
            );
            user.setEmailVerificationSentAt(Instant.now());

            userRepository.save(user);
            emailVerificationService.sendVerificationEmail(user, token).block();
        });
    }

    // -------------------- PASSWORD RESET --------------------

    @Override
    public Mono<Void> forgotPassword(String email) {
        return ReactorBlocking.run(() -> {
            if (email == null || email.isBlank()) return;

            userRepository.findByEmail(email.toLowerCase().trim()).ifPresent(user -> {
                if (user.getPasswordResetRequestedAt() != null && authProps.resetCooldownMinutes() > 0) {
                    Instant nextAllowed = user.getPasswordResetRequestedAt()
                            .plus(authProps.resetCooldownMinutes(), ChronoUnit.MINUTES);
                    if (nextAllowed.isAfter(Instant.now())) {
                        throw new TooManyRequestsException("Password reset recently requested. Please try again later.");
                    }
                }

                String raw = UUID.randomUUID().toString();
                user.setPasswordResetTokenHash(sha256(raw));
                user.setPasswordResetTokenExpiry(
                        Instant.now().plus(authProps.resetTokenMinutes(), ChronoUnit.MINUTES)
                );
                user.setPasswordResetRequestedAt(Instant.now());

                userRepository.save(user);
                emailVerificationService.sendPasswordResetEmail(user, raw).block();
            });
        });
    }

    @Override
    public Mono<Void> resetPassword(String token, String newPassword) {
        return ReactorBlocking.run(() -> {
            if (token == null || token.isBlank()) throw new IllegalArgumentException("Reset token is required");
            if (newPassword == null || newPassword.isBlank()) throw new IllegalArgumentException("New password is required");

            String hash = sha256(token.trim());

            User user = userRepository.findByPasswordResetTokenHash(hash)
                    .orElseThrow(() -> new NotFoundException("Invalid or expired reset token"));

            if (user.getPasswordResetTokenExpiry() == null || user.getPasswordResetTokenExpiry().isBefore(Instant.now())) {
                throw new IllegalArgumentException("Reset token expired");
            }

            user.setPassword(passwordEncoder.encode(newPassword));
            user.setPasswordResetTokenHash(null);
            user.setPasswordResetTokenExpiry(null);
            user.setPasswordResetRequestedAt(null);

            userRepository.save(user);

            // logout everywhere: revoke all active refresh tokens
            List<RefreshToken> tokens = refreshTokenRepository.findAllByUserIdAndRevokedFalse(user.getId());
            for (RefreshToken t : tokens) {
                t.setRevoked(true);
            }
            refreshTokenRepository.saveAll(tokens);
        });
    }

    // -------------------- 2FA (stubs that compile) --------------------
    // These compile, but will throw until you add:
    // - somewhere to store a TOTP secret per user
    // - somewhere to store + validate a loginChallengeToken
    // - and the actual TOTP verification logic (code check)

    @Override
    public Mono<Setup2faResponseDto> setup2fa(Long userId) {
        return Mono.error(new UnsupportedOperationException(
                "setup2fa not implemented yet: add User fields (mfaSecret/mfaEnabled) or a separate 2FA table, " +
                        "then generate a TOTP secret + return Setup2faResponseDto (qr/otpauth)."
        ));
    }

    @Override
    public Mono<Void> confirm2fa(Long userId, String code) {
        return Mono.error(new UnsupportedOperationException(
                "confirm2fa not implemented yet: verify TOTP code against stored secret, then enable MFA."
        ));
    }

    @Override
    public Mono<AuthCookieResponse> verifyLogin2fa(String loginChallengeToken, String code) {
        return Mono.error(new UnsupportedOperationException(
                "verifyLogin2fa not implemented yet: validate challenge token + code, then issue access token + refresh cookie."
        ));
    }

    @Override
    public Mono<Void> disable2fa(Long userId, String password, String code) {
        return Mono.error(new UnsupportedOperationException(
                "disable2fa not implemented yet: verify password + TOTP code, then disable MFA and clear secret."
        ));
    }

    // -------------------- helpers --------------------

    private User authenticate(LoginRequestDto dto) {
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

        return user;
    }

    private void saveRefreshToken(Long userId, String rawToken, Instant expiresAt, boolean rememberMe) {
        RefreshToken t = new RefreshToken();
        t.setUserId(userId);
        t.setTokenHash(sha256(rawToken));
        t.setExpiresAt(expiresAt);
        t.setRevoked(false);
        t.setRememberMe(rememberMe);
        refreshTokenRepository.save(t);
    }

    private ResponseCookie buildRefreshCookie(String raw, int days) {
        return ResponseCookie.from("refreshToken", raw)
                .httpOnly(true)
                .secure(false) // true in prod (HTTPS)
                .sameSite("Lax")
                .path("/api/auth")
                .maxAge(Duration.ofDays(days))
                .build();
    }

    private String sha256(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to hash token", e);
        }
    }

    private String issueAccessToken(User user) {
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

        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }
}