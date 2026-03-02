package com.sirhpitar.budget.dtos.request;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class UpdateProfileRequestDto {

    private String firstName;
    private String lastName;

    @Pattern(
            regexp = "^[A-Z]{3}$",
            message = "Currency must be a 3-letter code like USD, KES, EUR"
    )
    private String currency;

    private String timezone;

    private String profilePictureUrl;
}