package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.NotificationRequestDto;
import com.sirhpitar.budget.dtos.response.NotificationResponseDto;
import com.sirhpitar.budget.entities.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    Notification toEntity(NotificationRequestDto dto);

    NotificationResponseDto toDto(Notification notification);

    default Notification toEntity(NotificationRequestDto dto, Long userId) {
        Notification notification = toEntity(dto);
        notification.setUserId(userId);
        return notification;
    }
}
