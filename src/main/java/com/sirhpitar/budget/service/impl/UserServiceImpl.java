package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.UserResponseDto;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.exceptions.BadRequestException;
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
            String email = requireNonBlank(dto.getEmail(), "Email is required").toLowerCase().trim();
            String username = requireNonBlank(dto.getUsername(), "Username is required").trim();
            String rawPassword = requireNonBlank(dto.getPassword(), "Password is required");

            if (userRepository.findByEmail(email).isPresent()) {
                throw new BadRequestException("Email already in use");
            }

            if (userRepository.findByUsername(username).isPresent()) {
                throw new BadRequestException("Username already in use");
            }

            User user = userMapper.toEntity(dto);

            user.setEmail(email);
            user.setUsername(username);
            user.setPasswordHash(passwordEncoder.encode(rawPassword));

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

            if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
                String normalizedEmail = dto.getEmail().toLowerCase().trim();
                userRepository.findByEmail(normalizedEmail).ifPresent(user -> {
                    if (!user.getId().equals(id)) {
                        throw new BadRequestException("Email already in use");
                    }
                });
            }

            if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
                String normalizedUsername = dto.getUsername().trim();
                userRepository.findByUsername(normalizedUsername).ifPresent(user -> {
                    if (!user.getId().equals(id)) {
                        throw new BadRequestException("Username already in use");
                    }
                });
            }

            userMapper.updateUserFromDto(dto, existing);

            if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
                existing.setEmail(dto.getEmail().toLowerCase().trim());
            }

            if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
                existing.setUsername(dto.getUsername().trim());
            }

            if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
                existing.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
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

    private String requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(message);
        }
        return value;
    }
}