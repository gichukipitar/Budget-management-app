package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.dtos.request.ExpenseRequestDto;
import com.sirhpitar.budget.dtos.response.ExpenseResponseDto;
import com.sirhpitar.budget.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {
    @Override
    public Mono<ExpenseResponseDto> createExpense(ExpenseRequestDto dto) {
        return null;
    }

    @Override
    public Mono<ExpenseResponseDto> updateExpense(Long id, ExpenseRequestDto dto) {
        return null;
    }

    @Override
    public Mono<ExpenseResponseDto> getExpenseById(Long id) {
        return null;
    }

    @Override
    public Flux<ExpenseResponseDto> getAllExpenses() {
        return null;
    }

    @Override
    public Mono<Void> deleteExpense(Long id) {
        return null;
    }
}
