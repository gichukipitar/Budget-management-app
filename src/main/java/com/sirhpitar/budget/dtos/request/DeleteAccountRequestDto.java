package com.sirhpitar.budget.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeleteAccountRequestDto {
    @NotBlank
    private String password;

    private String confirm;
}