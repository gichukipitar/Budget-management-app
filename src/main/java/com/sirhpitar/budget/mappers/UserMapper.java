package com.sirhpitar.budget.mappers;

import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.UserResponseDto;
import com.sirhpitar.budget.entities.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    @Mapping(target = "emailVerificationTokenExpiry", ignore = true)
    @Mapping(target = "emailVerificationSentAt", ignore = true)
    @Mapping(target = "passwordResetTokenHash", ignore = true)
    @Mapping(target = "passwordResetTokenExpiry", ignore = true)
    @Mapping(target = "passwordResetRequestedAt", ignore = true)
    @Mapping(target = "twoFactorEnabled", ignore = true)
    @Mapping(target = "twoFactorSecret", ignore = true)
    User toEntity(UserRequestDto dto);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", ignore = true)
    @Mapping(target = "enabled", ignore = true)
    @Mapping(target = "failedLoginAttempts", ignore = true)
    @Mapping(target = "lockedUntil", ignore = true)
    @Mapping(target = "emailVerified", ignore = true)
    @Mapping(target = "emailVerificationToken", ignore = true)
    @Mapping(target = "emailVerificationTokenExpiry", ignore = true)
    @Mapping(target = "emailVerificationSentAt", ignore = true)
    @Mapping(target = "passwordResetTokenHash", ignore = true)
    @Mapping(target = "passwordResetTokenExpiry", ignore = true)
    @Mapping(target = "passwordResetRequestedAt", ignore = true)
    @Mapping(target = "twoFactorEnabled", ignore = true)
    @Mapping(target = "twoFactorSecret", ignore = true)
    void updateUserFromDto(UserRequestDto dto, @MappingTarget User user);

    UserResponseDto toDto(User user);
}
