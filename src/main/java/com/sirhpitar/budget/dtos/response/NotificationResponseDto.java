package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO for {@link com.sirhpitar.budget.entities.Notification}
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NotificationResponseDto {
    private Long id;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private String message;
    private boolean read;
}