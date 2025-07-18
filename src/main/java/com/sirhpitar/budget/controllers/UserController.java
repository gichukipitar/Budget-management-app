package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.UserResponseDto;
import com.sirhpitar.budget.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/create")
    public Mono<ResponseEntity<ApiResponse<UserResponseDto>>> createUser(@Valid @RequestBody UserRequestDto dto) {
        return userService.createUser(dto)
                .map(data -> ApiResponseUtil.success("User created successfully", data));
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<ApiResponse<List<UserResponseDto>>>> getAllUsers() {
        return userService.getAllUsers()
                .collectList()
                .map(list -> ApiResponseUtil.success("All users fetched successfully", list));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<UserResponseDto>>> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(data -> ApiResponseUtil.success("User fetched successfully", data));
    }

    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<ApiResponse<UserResponseDto>>> updateUser(@PathVariable Long id,
                                                                         @Valid @RequestBody UserRequestDto dto) {
        return userService.updateUser(id, dto)
                .map(data -> ApiResponseUtil.success("User updated successfully", data));
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                .thenReturn(ApiResponseUtil.success("User deleted successfully", null));
    }
}