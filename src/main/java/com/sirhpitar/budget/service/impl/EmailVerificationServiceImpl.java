package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.config.AuthProps;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
@Profile("!test")
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final AuthProps authProps;
    private final JavaMailSender mailSender;

    @Override
    public void sendVerificationEmail(User user, String token) {
        String baseUrl = authProps.verificationBaseUrl();
        String link = baseUrl + token;
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(user.getEmail());
        message.setFrom(authProps.verificationFrom());
        message.setSubject("Verify your email");
        message.setText("Welcome! Please verify your email by clicking this link: " + link);
        mailSender.send(message);
        log.info("Verification email sent to {}", user.getEmail());
    }
}
