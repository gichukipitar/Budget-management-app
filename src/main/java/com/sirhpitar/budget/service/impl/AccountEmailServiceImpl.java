package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.config.AuthProps;
import com.sirhpitar.budget.service.AccountEmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class AccountEmailServiceImpl implements AccountEmailService {

    private final JavaMailSender mailSender;
    private final AuthProps authProps;

    private Mono<Void> send(String to, String subject, String body) {
        return Mono.fromRunnable(() -> {
                    SimpleMailMessage msg = new SimpleMailMessage();
                    msg.setFrom(authProps.verificationFrom());
                    msg.setTo(to);
                    msg.setSubject(subject);
                    msg.setText(body);
                    mailSender.send(msg); // blocking
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<Void> sendProfileChangedEmail(String toEmail, String message) {
        return send(toEmail, "Profile updated", "Your profile was updated.\n\n" + message);
    }

    @Override
    public Mono<Void> sendPasswordChangedEmail(String toEmail) {
        return send(toEmail, "Password changed",
                "Your password was changed. If this wasn't you, reset it immediately.");
    }

    @Override
    public Mono<Void> sendEmailChangeRequestedOldEmail(String oldEmail, String newEmail) {
        return send(oldEmail, "Email change requested",
                "A request was made to change your email to: " + newEmail
                        + "\nIf this wasn't you, please reset your password.");
    }

    @Override
    public Mono<Void> sendEmailChangeVerificationNewEmail(String newEmail, String verifyLink) {
        return send(newEmail, "Verify your new email",
                "Verify your new email by clicking:\n" + verifyLink);
    }

    @Override
    public Mono<Void> sendAccountDeletedEmail(String toEmail) {
        return send(toEmail, "Account deleted",
                "Your account has been deleted. If this wasn't you, contact support.");
    }
}