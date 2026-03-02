package com.sirhpitar.budget.service;

import com.sirhpitar.budget.entities.User;
import reactor.core.publisher.Mono;

public interface EmailVerificationService {
    Mono<Void> sendVerificationEmail(User user, String token);
}