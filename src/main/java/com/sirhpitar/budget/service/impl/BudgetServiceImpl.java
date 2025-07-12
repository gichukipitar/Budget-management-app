package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.dtos.request.BudgetRequestDto;
import com.sirhpitar.budget.dtos.response.BudgetResponseDto;
import com.sirhpitar.budget.mappers.BudgetMapper;
import com.sirhpitar.budget.repository.BudgetRepository;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final BudgetMapper budgetMapper;

    @Override
    public Mono<BudgetResponseDto> createBudget(BudgetRequestDto dto) {
        return null;
    }

    @Override
    public Mono<BudgetResponseDto> getBudgetById(Long id) {
        return null;
    }

    @Override
    public Mono<BudgetResponseDto> updateBudget(Long id, BudgetRequestDto dto) {
        return null;
    }

    @Override
    public Mono<Void> deleteBudget(Long id) {
        return null;
    }

    @Override
    public Flux<BudgetResponseDto> getAllBudgets() {
        return null;
    }
}
