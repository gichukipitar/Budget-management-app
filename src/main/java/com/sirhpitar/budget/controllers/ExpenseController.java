package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseStatus;
import com.sirhpitar.budget.dtos.request.ExpenseRequestDto;
import com.sirhpitar.budget.dtos.response.ExpenseResponseDto;
import com.sirhpitar.budget.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
@Slf4j
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public Mono<ResponseEntity<ApiResponse<ExpenseResponseDto>>> createExpense(@RequestBody ExpenseRequestDto requestDto) {
        return expenseService.createExpense(requestDto)
                .map(expense -> ResponseEntity.status(HttpStatus.CREATED)
                        .body(new ApiResponse<>(ApiResponseStatus.SUCCESS, "Expense created successfully", expense)))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body(new ApiResponse<>(ApiResponseStatus.ERROR, "Failed to create expense: " + e.getMessage(), null))
                ));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ExpenseResponseDto>>> getExpenseById(@PathVariable Long id) {
        return expenseService.getExpenseById(id)
                .map(expense -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "Expense retrieved successfully", expense)))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ApiResponse<>(ApiResponseStatus.NOT_FOUND, e.getMessage(), null)
                                )
                ));

    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ExpenseResponseDto>>> updateExpense(@PathVariable Long id, @RequestBody ExpenseRequestDto requestDto) {
        return expenseService.updateExpense(id, requestDto)
                .map(expense -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "Expense updated successfully", expense)))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ApiResponse<>(ApiResponseStatus.NOT_FOUND, e.getMessage(), null))
                ));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<ExpenseResponseDto>>>> getAllExpenses() {
        return expenseService.getAllExpenses()
                .collectList()
                .map(expenses -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "All expenses retrieved successfully", expenses)
                ));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Object>>> deleteExpense(@PathVariable Long id) {
        return expenseService.deleteExpense(id)
                .thenReturn(ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "Expense deleted successfully", null)))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body(new ApiResponse<>(ApiResponseStatus.NOT_FOUND, e.getMessage(), null))
                ));
    }
}