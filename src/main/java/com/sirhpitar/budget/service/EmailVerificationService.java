package com.sirhpitar.budget.service;

import com.sirhpitar.budget.entities.User;
import reactor.core.publisher.Mono;

public interface EmailVerificationService {
    Mono<Void> sendVerificationEmail(String email, String firstName, String token);

    Mono<Void> sendPasswordResetEmail(String email, String firstName, String rawToken);
}