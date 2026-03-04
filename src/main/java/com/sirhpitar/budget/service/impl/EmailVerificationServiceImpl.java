package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.config.AuthProps;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final JavaMailSender mailSender;
    private final AuthProps authProps;

    @Override
    public Mono<Void> sendVerificationEmail(User user, String token) {
        return Mono.fromRunnable(() -> {
                    String link = authProps.verificationBaseUrl() + token;

                    SimpleMailMessage msg = new SimpleMailMessage();
                    msg.setFrom(authProps.verificationFrom());
                    msg.setTo(user.getEmail());
                    msg.setSubject("Verify your email");
                    msg.setText("Welcome! Please verify your email by clicking this link: " + link);

                    mailSender.send(msg); // blocking
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }

    @Override
    public Mono<Void> sendPasswordResetEmail(User user, String rawToken) {
        return Mono.fromRunnable(() -> {
                    String link = authProps.resetBaseUrl() + rawToken;

                    SimpleMailMessage msg = new SimpleMailMessage();
                    msg.setFrom(authProps.verificationFrom());
                    msg.setTo(user.getEmail());
                    msg.setSubject("Reset your password");
                    msg.setText(
                            "You requested a password reset.\n\n" +
                                    "Click this link to reset your password: " + link + "\n\n" +
                                    "If you did not request this, ignore this email."
                    );

                    mailSender.send(msg); // blocking
                })
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}