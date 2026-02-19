package com.sirhpitar.budget.dtos.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileDto {
    @NotBlank private String firstName;
    @NotBlank
    private String lastName;

    private String currency;
    private String timezone;
}

