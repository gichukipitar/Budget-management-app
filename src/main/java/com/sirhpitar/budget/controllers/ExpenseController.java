package com.sirhpitar.budget.controllers;


import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
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
                .map(expense -> ApiResponseUtil.created("Expense created successfully", expense))
                .onErrorResume(e -> Mono.just(ApiResponseUtil.error(HttpStatus.BAD_REQUEST, "Failed to create expense: " + e.getMessage())));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ExpenseResponseDto>>> getExpenseById(@PathVariable Long id) {
        return expenseService.getExpenseById(id)
                .map(expense -> ApiResponseUtil.success("Expense retrieved successfully", expense))
                .switchIfEmpty(Mono.just(ApiResponseUtil.notFound("Expense not found")));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ExpenseResponseDto>>> updateExpense(@PathVariable Long id, @RequestBody ExpenseRequestDto requestDto) {
        return expenseService.updateExpense(id, requestDto)
                .map(expense -> ApiResponseUtil.success("Expense updated successfully", expense))
                .onErrorResume(e -> Mono.just(ApiResponseUtil.notFound(e.getMessage())));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<ExpenseResponseDto>>>> getAllExpenses() {
        return expenseService.getAllExpenses()
                .collectList()
                .map(expenses -> ApiResponseUtil.success("All expenses retrieved successfully", expenses));
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteExpense(@PathVariable Long id) {
        return expenseService.deleteExpense(id)
                .then(Mono.just(ApiResponseUtil.success("Expense deleted successfully", (Void) null)))
                .onErrorResume(e -> Mono.just(ApiResponseUtil.notFound(e.getMessage())));
    }
}