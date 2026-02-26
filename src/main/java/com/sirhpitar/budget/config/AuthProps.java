package com.sirhpitar.budget.config;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.auth")
public record AuthProps(
        int maxFailedAttempts,
        long lockMinutes,
        long verificationTokenMinutes,
        String verificationBaseUrl,
        String verificationFrom,
        long resendCooldownMinutes
) {}
