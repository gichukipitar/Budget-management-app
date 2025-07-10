package com.sirhpitar.budget.service;

import com.sirhpitar.budget.dtos.request.ExpenseRequestDto;
import com.sirhpitar.budget.dtos.response.ExpenseResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ExpenseService {
    Mono<ExpenseResponseDto> createExpense(ExpenseRequestDto dto);

    Mono<ExpenseResponseDto> updateExpense(Long id, ExpenseRequestDto dto);

    Mono<ExpenseResponseDto> getExpenseById(Long id);

    Flux<ExpenseResponseDto> getAllExpenses();

    Mono<Void> deleteExpense(Long id);
}
