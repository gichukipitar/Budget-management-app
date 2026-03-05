package com.sirhpitar.budget.dtos.request;

import com.sirhpitar.budget.dtos.validation.ValidationPatterns;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class Disable2faRequestDto {

    @NotBlank
    @Size(max = 200)
    private String password;

    @NotBlank
    @Pattern(regexp = ValidationPatterns.OTP_6_DIGITS, message = "Code must be 6 digits")
    private String code;
}