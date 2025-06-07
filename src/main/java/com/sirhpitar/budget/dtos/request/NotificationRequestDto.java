package com.sirhpitar.budget.dtos.request;

import com.sirhpitar.budget.entities.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO for {@link com.sirhpitar.budget.entities.Notification}
 */
@AllArgsConstructor
@Getter
public class NotificationRequestDto {
    private User user;
    private String message;
}