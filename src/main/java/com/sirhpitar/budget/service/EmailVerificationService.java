package com.sirhpitar.budget.service;

import com.sirhpitar.budget.entities.User;

public interface EmailVerificationService {
    void sendVerificationEmail(User user, String token);
}
