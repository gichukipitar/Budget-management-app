package com.sirhpitar.budget.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for {@link com.sirhpitar.budget.entities.Notification}
 */
@AllArgsConstructor
@Getter
@Setter
@Builder
public class NotificationResponseDto {
    private Long id;
    private String message;
    private boolean read;
    private Long userId;
}