package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.UserResponseDto;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.exceptions.NotFoundException;
import com.sirhpitar.budget.mappers.UserMapper;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.UserService;
import com.sirhpitar.budget.utils.ReactorBlocking;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Mono<UserResponseDto> createUser(UserRequestDto dto) {
        return ReactorBlocking.mono(() -> {
            // Map basic fields
            User user = userMapper.toEntity(dto);

            // Normalize
            user.setEmail(dto.getEmail().toLowerCase().trim());
            user.setUsername(dto.getUsername().trim());

            // Hash password (CRITICAL)
            user.setPassword(passwordEncoder.encode(dto.getPassword()));

            // Default security flags (in case entity defaults aren’t applied for some reason)
            user.setEnabled(true);
            user.setFailedLoginAttempts(0);
            user.setLockedUntil(null);

            User saved = userRepository.save(user);
            return userMapper.toDto(saved);
        });
    }

    @Override
    public Flux<UserResponseDto> getAllUsers() {
        return ReactorBlocking.mono(userRepository::findAll)
                .flatMapMany(Flux::fromIterable)
                .map(userMapper::toDto);
    }

    @Override
    public Mono<UserResponseDto> getUserById(Long id) {
        return ReactorBlocking.mono(() ->
                userRepository.findById(id)
                        .map(userMapper::toDto)
                        .orElseThrow(() -> new NotFoundException("User not found"))
        );
    }

    @Override
    public Mono<UserResponseDto> updateUser(Long id, UserRequestDto dto) {
        return ReactorBlocking.mono(() -> {
            User existing = userRepository.findById(id)
                    .orElseThrow(() -> new NotFoundException("User not found"));

            // Update basic fields only
            userMapper.updateUserFromDto(dto, existing);

            existing.setEmail(dto.getEmail().toLowerCase().trim());
            existing.setUsername(dto.getUsername().trim());

            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                existing.setPassword(passwordEncoder.encode(dto.getPassword()));
            }

            User updated = userRepository.save(existing);
            return userMapper.toDto(updated);
        });
    }


    @Override
    public Mono<Void> deleteUser(Long id) {
        return ReactorBlocking.run(() -> {
            if (!userRepository.existsById(id)) {
                throw new NotFoundException("User not found");
            }
            userRepository.deleteById(id);
        });
    }
}
