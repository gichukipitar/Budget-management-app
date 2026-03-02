package com.sirhpitar.budget.service;

import reactor.core.publisher.Mono;

public interface AccountEmailService {
    Mono<Void> sendProfileChangedEmail(String toEmail, String message);
    Mono<Void> sendPasswordChangedEmail(String toEmail);
    Mono<Void> sendEmailChangeRequestedOldEmail(String oldEmail, String newEmail);
    Mono<Void> sendEmailChangeVerificationNewEmail(String newEmail, String verifyLink);
    Mono<Void> sendAccountDeletedEmail(String toEmail);
}