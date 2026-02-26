package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.config.AuthProps;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.service.EmailVerificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationServiceImpl implements EmailVerificationService {

    private final AuthProps authProps;

    @Override
    public void sendVerificationEmail(User user, String token) {
        String baseUrl = authProps.verificationBaseUrl();
        String link = baseUrl + token;
        log.info("Email verification link for {}: {}", user.getEmail(), link);
    }
}
