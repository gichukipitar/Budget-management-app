package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.NotificationRequestDto;
import com.sirhpitar.budget.dtos.response.NotificationResponseDto;
import com.sirhpitar.budget.entities.Notification;
import com.sirhpitar.budget.entities.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    Notification toEntity(NotificationRequestDto dto);

    @Mapping(target = "userId", source = "user.id")
    NotificationResponseDto toDto(Notification notification);

    default Notification toEntity(NotificationRequestDto dto, User user) {
        Notification notification = toEntity(dto);
        notification.setUser(user);
        return notification;
    }
}
