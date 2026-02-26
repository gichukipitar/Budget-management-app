package com.sirhpitar.budget.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.AssertTrue;
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
            regexp = "^(?=.*[!@#$%^&*()_+\\-={}\\[\\]:;\"'<>,.?/]).{8,}$",
            message = "Password must be at least 8 characters and contain one special character"
    )
    private String password;

    @NotBlank private String firstName;
    @NotBlank private String lastName;

    // must be true
    @AssertTrue(message = "Terms of service must be accepted")
    private boolean termsAccepted;
}
