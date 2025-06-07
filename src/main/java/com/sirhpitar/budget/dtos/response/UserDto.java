package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO for {@link com.sirhpitar.budget.entities.User}
 */
@AllArgsConstructor
@Getter
public class UserDto {
    private LocalDateTime updatedAt;
    private String username;
    private String email;
}