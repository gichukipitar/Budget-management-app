package com.sirhpitar.budget.dtos.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequestDto {

    @NotBlank
    @Size(max = 254)
    private String emailOrUsername;

    @NotBlank
    @Size(max = 200)
    private String password;

    private boolean rememberMe;
}