package com.sirhpitar.budget.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDto {
    /**
     * Allow login by either email OR username.
     */
    @NotBlank
    private String identifier;

    @NotBlank
    private String password;
}
