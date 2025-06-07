package com.sirhpitar.budget.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO for {@link com.sirhpitar.budget.entities.User}
 */
@AllArgsConstructor
@Getter
public class UserDto {
    private String username;
    private String password;
    private String email;
}