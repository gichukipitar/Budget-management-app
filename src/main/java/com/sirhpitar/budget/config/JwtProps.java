package com.sirhpitar.budget.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.security.jwt")
public record JwtProps(
        String issuer,
        String secret,
        long accessTokenMinutes
) {}
