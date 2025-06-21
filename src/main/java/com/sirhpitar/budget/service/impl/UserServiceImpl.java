package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.UserResponseDto;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.mappers.UserMapper;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;


    @Override
    public Mono<UserResponseDto> createUser(UserRequestDto dto) {
        return Mono.fromCallable(() -> {
            User user = userMapper.toEntity(dto);
            User saved = userRepository.save(user);
            return userMapper.toDto(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<UserResponseDto> getUserById(Long id) {
        return Mono.fromCallable(() ->
                userRepository.findById(id)
                        .map(userMapper::toDto)
                        .orElseThrow(() -> new RuntimeException("User not found"))
        ).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<UserResponseDto> getAllUsers() {
        return Mono.fromCallable(userRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(Flux::fromIterable)
                .map(userMapper::toDto);
    }

    @Override
    public Mono<UserResponseDto> updateUser(Long id, UserRequestDto dto) {
        return Mono.fromCallable(() -> {
            User user = userRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            user.setUsername(dto.getUsername());
            user.setEmail(dto.getEmail());

            User updated = userRepository.save(user);
            return userMapper.toDto(updated);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteUser(Long id) {
        return Mono.fromRunnable(() -> userRepository.deleteById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
