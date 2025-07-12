package com.sirhpitar.budget.service;

import com.sirhpitar.budget.dtos.request.BudgetRequestDto;
import com.sirhpitar.budget.dtos.response.BudgetResponseDto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BudgetService {
    Mono<BudgetResponseDto> createBudget(BudgetRequestDto dto);

    Mono<BudgetResponseDto> getBudgetById(Long id);

    Mono<BudgetResponseDto> updateBudget(Long id, BudgetRequestDto dto);

    Mono<Void> deleteBudget(Long id);

    Flux<BudgetResponseDto> getAllBudgets();
}
