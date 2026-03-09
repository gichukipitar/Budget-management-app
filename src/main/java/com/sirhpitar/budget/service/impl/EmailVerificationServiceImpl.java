package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.config.AuthProps;
import com.sirhpitar.budget.service.EmailVerificationService;
import com.sirhpitar.budget.utils.ReactorBlocking;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final JavaMailSender mailSender;
    private final AuthProps authProps;

    @Override
    public Mono<Void> sendVerificationEmail(String email, String firstName, String token) {
        return ReactorBlocking.run(() -> {
            String link = authProps.verificationBaseUrl() + token;

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(authProps.verificationFrom());
            msg.setTo(email);
            msg.setSubject("Verify your email");
            msg.setText(
                    "Hello " + safeName(firstName) + ",\n\n" +
                            "Welcome! Please verify your email by clicking this link:\n" +
                            link + "\n\n" +
                            "If you did not create this account, you can ignore this email."
            );

            mailSender.send(msg);
        });
    }

    @Override
    public Mono<Void> sendPasswordResetEmail(String email, String firstName, String rawToken) {
        return ReactorBlocking.run(() -> {
            String link = authProps.resetBaseUrl() + rawToken;

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setFrom(authProps.verificationFrom());
            msg.setTo(email);
            msg.setSubject("Reset your password");
            msg.setText(
                    "Hello " + safeName(firstName) + ",\n\n" +
                            "You requested a password reset.\n\n" +
                            "Click this link to reset your password:\n" +
                            link + "\n\n" +
                            "If you did not request this, ignore this email."
            );

            mailSender.send(msg);
        });
    }

    private String safeName(String firstName) {
        if (firstName == null || firstName.isBlank()) {
            return "there";
        }
        return firstName.trim();
    }
}