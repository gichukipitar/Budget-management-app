package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.apis.ApiResponse;
import com.sirhpitar.budget.apis.ApiResponseStatus;
import com.sirhpitar.budget.dtos.request.UserRequestDto;
import com.sirhpitar.budget.dtos.response.UserResponseDto;
import com.sirhpitar.budget.service.UserService;
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
    public Mono<ResponseEntity<ApiResponse<UserResponseDto>>> createUser(@RequestBody UserRequestDto dto) {
        return userService.createUser(dto)
                .map(data -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "User created successfully", data)))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest().body(
                                new ApiResponse<>(ApiResponseStatus.ERROR, e.getMessage(), null))
                ));
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<ApiResponse<List<UserResponseDto>>>> getAllUsers() {
        return userService.getAllUsers()
                .collectList()
                .map(list -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "All users fetched successfully", list)
                ));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<UserResponseDto>>> getUser(@PathVariable Long id) {
        return userService.getUserById(id)
                .map(data -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "User fetched successfully", data)))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(404).body(
                                new ApiResponse<>(ApiResponseStatus.NOT_FOUND, e.getMessage(), null))
                ));
    }

    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<ApiResponse<UserResponseDto>>> updateUser(@PathVariable Long id,
                                                                         @RequestBody UserRequestDto dto) {
        return userService.updateUser(id, dto)
                .map(data -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "User updated successfully", data)))
                .onErrorResume(e -> {
                    String message = e.getMessage();
                    ApiResponseStatus status = ApiResponseStatus.ERROR;
                    if ("User not found".equals(message)) {
                        status = ApiResponseStatus.NOT_FOUND;
                    }
                    return Mono.just(ResponseEntity.badRequest().body(
                            new ApiResponse<>(status, message, null)
                    ));
                });
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteUser(@PathVariable Long id) {
        return userService.deleteUser(id)
                .then(Mono.just(ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "User deleted successfully", (Void) null))))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        new ApiResponse<>(ApiResponseStatus.ERROR, e.getMessage(), null)
                )));
    }
}