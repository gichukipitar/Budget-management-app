package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.config.AuthProps;
import com.sirhpitar.budget.config.JwtProps;
import com.sirhpitar.budget.dtos.request.LoginRequestDto;
import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.AuthResponseDto;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.exceptions.NotFoundException;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.AuthService;
import com.sirhpitar.budget.utils.ReactorBlocking;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtEncoder jwtEncoder;
    private final JwtProps jwtProps;
    private final AuthProps authProps;

    @Override
    public Mono<AuthResponseDto> register(UserRequestDto dto) {
        return ReactorBlocking.mono(() -> {
            String email = dto.getEmail().toLowerCase().trim();
            String username = dto.getUsername().trim();

            userRepository.findByEmail(email).ifPresent(u -> {
                throw new IllegalArgumentException("Email already in use");
            });
            userRepository.findByUsername(username).ifPresent(u -> {
                throw new IllegalArgumentException("Username already in use");
            });

            User user = new User();
            user.setEmail(email);
            user.setUsername(username);
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
            user.setFirstName(dto.getFirstName().trim());
            user.setLastName(dto.getLastName().trim());
            user.setTermsAccepted(dto.isTermsAccepted());

            user.setEnabled(true);

            User saved = userRepository.save(user);
            String token = issueToken(saved);

            return new AuthResponseDto(token, "Bearer");
        });
    }

    @Override
    public Mono<AuthResponseDto> login(LoginRequestDto dto) {
        return ReactorBlocking.mono(() -> {
            String identifier = dto.getIdentifier().trim();

            User user = userRepository.findByEmail(identifier.toLowerCase())
                    .orElseGet(() -> userRepository.findByUsername(identifier)
                            .orElseThrow(() -> new NotFoundException("Invalid credentials")));

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
