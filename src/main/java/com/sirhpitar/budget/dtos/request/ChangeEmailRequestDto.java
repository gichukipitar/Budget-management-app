package com.sirhpitar.budget.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class ChangeEmailRequestDto {
    @NotBlank @Email
    private String newEmail;

    @NotBlank
    private String password;
}