package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
import com.sirhpitar.budget.dtos.request.ExpenseRequestDto;
import com.sirhpitar.budget.dtos.response.ExpenseResponseDto;
import com.sirhpitar.budget.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    public Mono<ResponseEntity<ApiResponse<ExpenseResponseDto>>> createExpense(@Valid @RequestBody ExpenseRequestDto requestDto) {
        return expenseService.createExpense(requestDto)
                .map(expense -> ApiResponseUtil.created("Expense created successfully", expense));

    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ExpenseResponseDto>>> getExpenseById(@PathVariable Long id) {
        return expenseService.getExpenseById(id)
                .map(expense -> ApiResponseUtil.success("Expense retrieved successfully", expense))
                .switchIfEmpty(Mono.just(ApiResponseUtil.notFound("Expense not found")));
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<ExpenseResponseDto>>> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseRequestDto requestDto) {
        return expenseService.updateExpense(id, requestDto)
                .map(expense -> ApiResponseUtil.success("Expense updated successfully", expense));
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
                .then(Mono.just(ApiResponseUtil.success("Expense deleted successfully", null)));
    }
}