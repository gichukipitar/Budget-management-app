package com.sirhpitar.budget.dtos.request;

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
public class NotificationRequestDto {
    private Long userId;
    private String message;
}