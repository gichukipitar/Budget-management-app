package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO for {@link com.sirhpitar.budget.entities.User}
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class UserResponseDto {
    private Long id;
    private LocalDateTime updatedAt;
    private String username;
    private String email;
}