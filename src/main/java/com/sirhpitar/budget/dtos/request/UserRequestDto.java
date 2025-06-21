package com.sirhpitar.budget.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link com.sirhpitar.budget.entities.User}
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserRequestDto {
    private String username;
    private String password;
    private String email;
}