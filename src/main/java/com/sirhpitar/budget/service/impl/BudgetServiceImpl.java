package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.dtos.request.BudgetRequestDto;
import com.sirhpitar.budget.dtos.response.BudgetResponseDto;
import com.sirhpitar.budget.entities.Budget;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.exceptions.NotFoundException;
import com.sirhpitar.budget.mappers.BudgetMapper;
import com.sirhpitar.budget.repository.BudgetRepository;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final BudgetMapper budgetMapper;

    @Override
    public Mono<BudgetResponseDto> createBudget(BudgetRequestDto dto) {
        return Mono.fromCallable(() -> createBudgetInternal(dto))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<BudgetResponseDto> getBudgetById(Long id) {
        return Mono.fromCallable(() -> getBudgetByIdInternal(id))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<BudgetResponseDto> updateBudget(Long id, BudgetRequestDto dto) {
        return Mono.fromCallable(() -> updateBudgetInternal(id, dto))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Void> deleteBudget(Long id) {
        return Mono.fromRunnable(() -> deleteBudgetInternal(id))
                .subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Flux<BudgetResponseDto> getAllBudgets() {
        return Mono.fromCallable(() -> {
                    User user = getCurrentUser();
                    return budgetRepository.findByUser(user);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(budgets -> Flux.fromIterable(budgets)
                        .map(budgetMapper::toDto));
    }

    // --- Private helper methods ---

    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    private BudgetResponseDto createBudgetInternal(BudgetRequestDto dto) {
        User user = getCurrentUser();
        Budget budget = budgetMapper.toEntity(dto, user);
        Budget savedBudget = budgetRepository.save(budget);
        return budgetMapper.toDto(savedBudget);
    }

    private BudgetResponseDto getBudgetByIdInternal(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Budget not found"));
        return budgetMapper.toDto(budget);
    }

    private BudgetResponseDto updateBudgetInternal(Long id, BudgetRequestDto dto) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Budget not found"));

        User currentUser = getCurrentUser();
        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Not your budget!");
        }

        budgetMapper.updateBudgetFromDto(dto, budget);
        Budget savedBudget = budgetRepository.save(budget);
        return budgetMapper.toDto(savedBudget);
    }

    private void deleteBudgetInternal(Long id) {
        Budget budget = budgetRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Budget not found"));
        User currentUser = getCurrentUser();
        if (!budget.getUser().getId().equals(currentUser.getId())) {
            throw new NotFoundException("Not your budget!");
        }
        budgetRepository.deleteById(id);
    }
}