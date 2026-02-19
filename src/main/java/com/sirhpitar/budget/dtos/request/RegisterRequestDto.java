package com.sirhpitar.budget.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequestDto {
    @NotBlank
    private String username;

    @NotBlank @Email
    private String email;

    @NotBlank
    @Pattern(
            regexp = "^(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,}$",
            message = "Password must be at least 8 characters, contain one uppercase letter, one number, and one special character"
    )
    private String password;

    @NotBlank private String firstName;
    @NotBlank private String lastName;

    // must be true
    private boolean termsAccepted;
}

