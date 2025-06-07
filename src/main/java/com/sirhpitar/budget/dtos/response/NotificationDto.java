package com.sirhpitar.budget.dtos.response;

import com.sirhpitar.budget.dtos.request.UserDto;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * DTO for {@link com.sirhpitar.budget.entities.Notification}
 */
@AllArgsConstructor
@Getter
public class NotificationDto {
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private UserDto user;
    private String message;
}