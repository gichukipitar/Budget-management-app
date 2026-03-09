package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.config.AuthProps;
import com.sirhpitar.budget.config.JwtProps;
import com.sirhpitar.budget.dtos.request.LoginRequestDto;
import com.sirhpitar.budget.dtos.request.RegisterRequestDto;
import com.sirhpitar.budget.dtos.response.AuthCookieResponse;
import com.sirhpitar.budget.dtos.response.AuthResponseDto;
import com.sirhpitar.budget.dtos.response.Setup2faResponseDto;
import com.sirhpitar.budget.entities.LoginChallenge;
import com.sirhpitar.budget.entities.RefreshToken;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.exceptions.BadRequestException;
import com.sirhpitar.budget.exceptions.NotFoundException;
import com.sirhpitar.budget.exceptions.TooManyRequestsException;
import com.sirhpitar.budget.repository.LoginChallengeRepository;
import com.sirhpitar.budget.repository.RefreshTokenRepository;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.AuthService;
import com.sirhpitar.budget.service.EmailVerificationService;
import com.sirhpitar.budget.utils.ReactorBlocking;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final long LOGIN_CHALLENGE_MINUTES = 10;
    private static final int TOTP_STEP_SECONDS = 30;
    private static final int TOTP_DIGITS = 6;
    private static final int TOTP_WINDOW_STEPS = 1;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final LoginChallengeRepository loginChallengeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProps jwtProps;
    private final AuthProps authProps;
    private final EmailVerificationService emailVerificationService;
    private final String jwkKeyId;

    @Override
    public Mono<Void> register(RegisterRequestDto dto) {
        return ReactorBlocking.run(() -> {
            String email = requireNonBlank(dto.getEmail(), "Email is required").toLowerCase().trim();
            String username = requireNonBlank(dto.getUsername(), "Username is required").trim();
            String firstName = requireNonBlank(dto.getFirstName(), "First name is required").trim();
            String lastName = requireNonBlank(dto.getLastName(), "Last name is required").trim();
            String rawPassword = requireNonBlank(dto.getPassword(), "Password is required");

            userRepository.findByEmail(email).ifPresent(u -> {
                throw new BadRequestException("Email already in use");
            });

            userRepository.findByUsername(username).ifPresent(u -> {
                throw new BadRequestException("Username already in use");
            });

            if (!dto.isTermsAccepted()) {
                throw new BadRequestException("Terms of service must be accepted");
            }

            User user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            user.setFirstName(firstName);
            user.setLastName(lastName);
            user.setTermsAccepted(true);
            user.setEnabled(false);
            user.setEmailVerified(false);

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
    public Mono<AuthCookieResponse> login(LoginRequestDto dto) {
        return ReactorBlocking.mono(() -> {
            User user = authenticate(dto);
            boolean rememberMe = dto.isRememberMe();

            if (user.isTwoFactorEnabled()) {
                String rawChallenge = UUID.randomUUID().toString();

                LoginChallenge challenge = new LoginChallenge();
                challenge.setUserId(user.getId());
                challenge.setTokenHash(sha256(rawChallenge));
                challenge.setRememberMe(rememberMe);
                challenge.setExpiresAt(Instant.now().plus(LOGIN_CHALLENGE_MINUTES, ChronoUnit.MINUTES));
                challenge.setUsed(false);

                loginChallengeRepository.save(challenge);

                AuthResponseDto body = new AuthResponseDto(
                        null,
                        "Bearer",
                        true,
                        rawChallenge
                );

                return new AuthCookieResponse(body, null);
            }

            String accessToken = issueAccessToken(user);

            int days = rememberMe ? authProps.refreshDaysRememberMe() : authProps.refreshDaysDefault();
            String refreshRaw = UUID.randomUUID().toString();
            saveRefreshToken(user.getId(), refreshRaw, Instant.now().plus(days, ChronoUnit.DAYS), rememberMe);

            return new AuthCookieResponse(
                    new AuthResponseDto(accessToken, "Bearer", false, null),
                    buildRefreshCookie(refreshRaw, days)
            );
        });
    }

    @Override
    public Mono<AuthCookieResponse> refresh(String refreshToken) {
        return ReactorBlocking.mono(() -> {
            if (refreshToken == null || refreshToken.isBlank()) {
                throw new BadRequestException("Refresh token is required");
            }

            String hash = sha256(refreshToken.trim());

            RefreshToken existing = refreshTokenRepository.findByTokenHash(hash)
                    .orElseThrow(() -> new NotFoundException("Invalid refresh token"));

            if (existing.isRevoked()) {
                throw new BadRequestException("Refresh token revoked");
            }

            if (existing.getExpiresAt() == null || existing.getExpiresAt().isBefore(Instant.now())) {
                throw new BadRequestException("Refresh token expired");
            }

            User user = userRepository.findById(existing.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found"));

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
            if (refreshToken == null || refreshToken.isBlank()) {
                return;
            }

            String hash = sha256(refreshToken.trim());
            refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
                token.setRevoked(true);
                refreshTokenRepository.save(token);
            });
        });
    }

    @Override
    public Mono<Void> verifyEmail(String token) {
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
        });
    }

    @Override
    public Mono<Void> resendVerification(String email) {
        return ReactorBlocking.run(() -> {
            if (email == null || email.isBlank()) {
                throw new BadRequestException("Email is required");
            }

            User user = userRepository.findByEmail(email.toLowerCase().trim())
                    .orElseThrow(() -> new NotFoundException("User not found"));

            if (user.isEmailVerified()) {
                throw new BadRequestException("Email already verified");
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
            user.setEmailVerificationTokenExpiry(
                    Instant.now().plus(authProps.verificationTokenMinutes(), ChronoUnit.MINUTES)
            );
            user.setEmailVerificationSentAt(Instant.now());

            userRepository.save(user);

            emailVerificationService
                    .sendVerificationEmail(user.getEmail(), user.getFirstName(), token)
                    .block();
        });
    }

    @Override
    public Mono<Void> forgotPassword(String email) {
        return ReactorBlocking.run(() -> {
            if (email == null || email.isBlank()) {
                return;
            }

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

                emailVerificationService
                        .sendPasswordResetEmail(user.getEmail(), user.getFirstName(), raw)
                        .block();
            });
        });
    }

    @Override
    public Mono<Void> resetPassword(String token, String newPassword) {
        return ReactorBlocking.run(() -> {
            if (token == null || token.isBlank()) {
                throw new BadRequestException("Reset token is required");
            }

            if (newPassword == null || newPassword.isBlank()) {
                throw new BadRequestException("New password is required");
            }

            String hash = sha256(token.trim());

            User user = userRepository.findByPasswordResetTokenHash(hash)
                    .orElseThrow(() -> new NotFoundException("Invalid or expired reset token"));

            if (user.getPasswordResetTokenExpiry() == null ||
                    user.getPasswordResetTokenExpiry().isBefore(Instant.now())) {
                throw new BadRequestException("Reset token expired");
            }

            user.setPasswordHash(passwordEncoder.encode(newPassword));
            user.setPasswordResetTokenHash(null);
            user.setPasswordResetTokenExpiry(null);
            user.setPasswordResetRequestedAt(null);

            userRepository.save(user);
            refreshTokenRepository.revokeAllActiveByUserId(user.getId());
        });
    }

    @Override
    public Mono<Setup2faResponseDto> setup2fa(Long userId) {
        return ReactorBlocking.mono(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            String secretBase32 = generateBase32Secret(20);
            user.setTwoFactorSecret(secretBase32);
            user.setTwoFactorEnabled(false);
            userRepository.save(user);

            String issuer = jwtProps.issuer();
            String label = issuer + ":" + user.getEmail();
            String otpAuthUrl = buildOtpAuthUrl(label, secretBase32, issuer);

            return new Setup2faResponseDto(otpAuthUrl);
        });
    }

    @Override
    public Mono<Void> confirm2fa(Long userId, String code) {
        return ReactorBlocking.run(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            if (user.getTwoFactorSecret() == null || user.getTwoFactorSecret().isBlank()) {
                throw new BadRequestException("2FA setup not started");
            }

            if (!verifyTotp(user.getTwoFactorSecret(), code)) {
                throw new BadRequestException("Invalid 2FA code");
            }

            user.setTwoFactorEnabled(true);
            userRepository.save(user);
        });
    }

    @Override
    public Mono<AuthCookieResponse> verifyLogin2fa(String loginChallengeToken, String code) {
        return ReactorBlocking.mono(() -> {
            if (loginChallengeToken == null || loginChallengeToken.isBlank()) {
                throw new BadRequestException("loginChallengeToken is required");
            }

            String hash = sha256(loginChallengeToken.trim());

            LoginChallenge challenge = loginChallengeRepository.findByTokenHash(hash)
                    .orElseThrow(() -> new NotFoundException("Invalid login challenge token"));

            if (challenge.isUsed()) {
                throw new BadRequestException("Login challenge already used");
            }

            if (challenge.getExpiresAt() == null || challenge.getExpiresAt().isBefore(Instant.now())) {
                throw new BadRequestException("Login challenge expired");
            }

            User user = userRepository.findById(challenge.getUserId())
                    .orElseThrow(() -> new NotFoundException("User not found"));

            if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
                throw new BadRequestException("2FA is not enabled on this account");
            }

            if (!verifyTotp(user.getTwoFactorSecret(), code)) {
                throw new BadRequestException("Invalid 2FA code");
            }

            challenge.setUsed(true);
            loginChallengeRepository.save(challenge);

            String accessToken = issueAccessToken(user);

            boolean rememberMe = challenge.isRememberMe();
            int days = rememberMe ? authProps.refreshDaysRememberMe() : authProps.refreshDaysDefault();

            String refreshRaw = UUID.randomUUID().toString();
            saveRefreshToken(user.getId(), refreshRaw, Instant.now().plus(days, ChronoUnit.DAYS), rememberMe);

            return new AuthCookieResponse(
                    new AuthResponseDto(accessToken, "Bearer", false, null),
                    buildRefreshCookie(refreshRaw, days)
            );
        });
    }

    @Override
    public Mono<Void> disable2fa(Long userId, String password, String code) {
        return ReactorBlocking.run(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                throw new BadRequestException("Invalid credentials");
            }

            if (!user.isTwoFactorEnabled() || user.getTwoFactorSecret() == null) {
                throw new BadRequestException("2FA is not enabled");
            }

            if (!verifyTotp(user.getTwoFactorSecret(), code)) {
                throw new BadRequestException("Invalid 2FA code");
            }

            user.setTwoFactorEnabled(false);
            user.setTwoFactorSecret(null);
            userRepository.save(user);

            refreshTokenRepository.revokeAllActiveByUserId(userId);
        });
    }

    private User authenticate(LoginRequestDto dto) {
        String identifier = requireNonBlank(dto.getEmailOrUsername(), "Email or username is required").trim();

        User user = userRepository.findByEmail(identifier.toLowerCase())
                .orElseGet(() -> userRepository.findByUsername(identifier)
                        .orElseThrow(() -> new BadRequestException("Invalid credentials")));

        if (!user.isEmailVerified()) {
            throw new BadRequestException("Email not verified");
        }

        if (!user.isEnabled()) {
            throw new BadRequestException("Account disabled");
        }

        if (user.getLockedUntil() != null && user.getLockedUntil().isAfter(Instant.now())) {
            throw new BadRequestException("Account locked. Try again later.");
        }

        boolean ok = passwordEncoder.matches(dto.getPassword(), user.getPasswordHash());
        if (!ok) {
            int fails = user.getFailedLoginAttempts() + 1;
            user.setFailedLoginAttempts(fails);

            if (fails >= authProps.maxFailedAttempts()) {
                user.setLockedUntil(Instant.now().plus(authProps.lockMinutes(), ChronoUnit.MINUTES));
                user.setFailedLoginAttempts(0);
            }

            userRepository.save(user);
            throw new BadRequestException("Invalid credentials");
        }

        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        userRepository.save(user);

        return user;
    }

    private void saveRefreshToken(Long userId, String rawToken, Instant expiresAt, boolean rememberMe) {
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUserId(userId);
        refreshToken.setTokenHash(sha256(rawToken));
        refreshToken.setExpiresAt(expiresAt);
        refreshToken.setRevoked(false);
        refreshToken.setRememberMe(rememberMe);
        refreshTokenRepository.save(refreshToken);
    }

    private ResponseCookie buildRefreshCookie(String raw, int days) {
        return ResponseCookie.from("refreshToken", raw)
                .httpOnly(true)
                .secure(false)
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

        JwsHeader headers = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId(jwkKeyId)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(headers, claims)).getTokenValue();
    }

    private boolean verifyTotp(String secretBase32, String code) {
        if (code == null) {
            return false;
        }

        String trimmed = code.trim();
        if (!trimmed.matches("^\\d{6}$")) {
            return false;
        }

        long nowSeconds = Instant.now().getEpochSecond();
        long counter = nowSeconds / TOTP_STEP_SECONDS;

        for (int i = -TOTP_WINDOW_STEPS; i <= TOTP_WINDOW_STEPS; i++) {
            String expected = totpAt(secretBase32, counter + i);
            if (expected.equals(trimmed)) {
                return true;
            }
        }

        return false;
    }

    private String totpAt(String secretBase32, long counter) {
        byte[] key = base32Decode(secretBase32);
        byte[] msg = new byte[8];
        long value = counter;

        for (int i = 7; i >= 0; i--) {
            msg[i] = (byte) (value & 0xFF);
            value >>= 8;
        }

        byte[] hmac = hmacSha1(key, msg);
        int offset = hmac[hmac.length - 1] & 0x0F;

        int binary =
                ((hmac[offset] & 0x7F) << 24) |
                        ((hmac[offset + 1] & 0xFF) << 16) |
                        ((hmac[offset + 2] & 0xFF) << 8) |
                        (hmac[offset + 3] & 0xFF);

        int otp = binary % (int) Math.pow(10, TOTP_DIGITS);
        return String.format("%0" + TOTP_DIGITS + "d", otp);
    }

    private byte[] hmacSha1(byte[] key, byte[] data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA1");
            mac.init(new SecretKeySpec(key, "HmacSHA1"));
            return mac.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("HMAC error", e);
        }
    }

    private String generateBase32Secret(int bytesLen) {
        byte[] bytes = new byte[bytesLen];
        SECURE_RANDOM.nextBytes(bytes);
        return base32Encode(bytes);
    }

    private String base32Encode(byte[] data) {
        final char[] alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567".toCharArray();
        StringBuilder out = new StringBuilder();

        int buffer = 0;
        int bitsLeft = 0;

        for (byte b : data) {
            buffer <<= 8;
            buffer |= (b & 0xFF);
            bitsLeft += 8;

            while (bitsLeft >= 5) {
                int index = (buffer >> (bitsLeft - 5)) & 0x1F;
                bitsLeft -= 5;
                out.append(alphabet[index]);
            }
        }

        if (bitsLeft > 0) {
            int index = (buffer << (5 - bitsLeft)) & 0x1F;
            out.append(alphabet[index]);
        }

        return out.toString();
    }

    private byte[] base32Decode(String value) {
        String input = value.replace("=", "").trim().toUpperCase();
        final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ234567";

        int buffer = 0;
        int bitsLeft = 0;
        byte[] out = new byte[input.length() * 5 / 8];
        int outPos = 0;

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            int val = alphabet.indexOf(c);

            if (val < 0) {
                continue;
            }

            buffer <<= 5;
            buffer |= val & 0x1F;
            bitsLeft += 5;

            if (bitsLeft >= 8) {
                out[outPos++] = (byte) ((buffer >> (bitsLeft - 8)) & 0xFF);
                bitsLeft -= 8;
            }
        }

        if (outPos == out.length) {
            return out;
        }

        byte[] trimmed = new byte[outPos];
        System.arraycopy(out, 0, trimmed, 0, outPos);
        return trimmed;
    }

    private String buildOtpAuthUrl(String label, String secret, String issuer) {
        try {
            String encLabel = URLEncoder.encode(label, StandardCharsets.UTF_8);
            String encIssuer = URLEncoder.encode(issuer, StandardCharsets.UTF_8);

            return "otpauth://totp/" + encLabel +
                    "?secret=" + secret +
                    "&issuer=" + encIssuer +
                    "&algorithm=SHA1&digits=" + TOTP_DIGITS +
                    "&period=" + TOTP_STEP_SECONDS;
        } catch (Exception e) {
            throw new RuntimeException("Failed to build otpauth URL", e);
        }
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value;
    }
}