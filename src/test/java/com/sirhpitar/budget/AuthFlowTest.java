package com.sirhpitar.budget;

import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class AuthFlowTest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private UserRepository userRepository;


    @Test
    void registrationVerificationLoginFlow() {
        Map<String, Object> registerPayload = Map.of(
                "username", "sarah",
                "email", "sarah@example.com",
                "password", "password!",
                "firstName", "Sarah",
                "lastName", "Young",
                "termsAccepted", true
        );

        webTestClient.post()
                .uri("/api/auth/register")
                .bodyValue(registerPayload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.meta.message").isEqualTo("Registration successful. Please verify your email.");

        User user = userRepository.findByEmail("sarah@example.com").orElseThrow();
        assertThat(user.isEmailVerified()).isFalse();
        assertThat(user.isEnabled()).isFalse();
        assertThat(user.getEmailVerificationToken()).isNotBlank();

        String firstToken = user.getEmailVerificationToken();

        Map<String, Object> resendPayload = Map.of(
                "email", "sarah@example.com"
        );

        webTestClient.post()
                .uri("/api/auth/resend-verification")
                .bodyValue(resendPayload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.meta.message").isEqualTo("Verification email resent");

        User afterResend = userRepository.findByEmail("sarah@example.com").orElseThrow();
        assertThat(afterResend.getEmailVerificationToken()).isNotBlank();
        assertThat(afterResend.getEmailVerificationToken()).isNotEqualTo(firstToken);
        String currentToken = afterResend.getEmailVerificationToken();

        Map<String, Object> loginPayload = Map.of(
                "identifier", "sarah@example.com",
                "password", "password!"
        );

        webTestClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginPayload)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.meta.message").isEqualTo("Email not verified");

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/auth/verify-email")
                        .queryParam("token", currentToken)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.meta.message").isEqualTo("Email verified successfully");

        webTestClient.post()
                .uri("/api/auth/login")
                .bodyValue(loginPayload)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.data.accessToken").isNotEmpty()
                .jsonPath("$.data.tokenType").isEqualTo("Bearer");
    }
}
