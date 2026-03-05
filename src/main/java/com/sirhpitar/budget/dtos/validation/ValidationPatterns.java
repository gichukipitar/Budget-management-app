package com.sirhpitar.budget.dtos.validation;

public final class ValidationPatterns {
    private ValidationPatterns() {}

    public static final String PASSWORD =
            "^(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,}$";

    public static final String OTP_6_DIGITS = "^\\d{6}$";
}