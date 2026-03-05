package com.sirhpitar.budget.dtos.request;

import com.sirhpitar.budget.dtos.validation.ValidationPatterns;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequestDto {

    @NotBlank
    @Size(min = 3, max = 30, message = "Username must be 3-30 characters")
    @Pattern(regexp = "^[a-zA-Z0-9._-]+$", message = "Username contains invalid characters")
    private String username;

    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Pattern(
            regexp = ValidationPatterns.PASSWORD,
            message = "Password must be at least 8 characters and contain one special character"
    )
    private String password;

    @NotBlank
    @Size(max = 60)
    private String firstName;

    @NotBlank
    @Size(max = 60)
    private String lastName;

    @AssertTrue(message = "Terms of service must be accepted")
    private boolean termsAccepted;
}