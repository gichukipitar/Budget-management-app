package com.sirhpitar.budget.config;

import com.sirhpitar.budget.service.EmailVerificationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
public class TestEmailVerificationConfig {

    @Bean
    public EmailVerificationService emailVerificationService() {
        return (user, token) -> {
            // no-op for tests
        };
    }
}
