package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.dtos.request.ExpenseRequestDto;
import com.sirhpitar.budget.dtos.response.ExpenseResponseDto;
import com.sirhpitar.budget.entities.Category;
import com.sirhpitar.budget.entities.Expense;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.mappers.ExpenseMapper;
import com.sirhpitar.budget.repository.CategoryRepository;
import com.sirhpitar.budget.repository.ExpenseRepository;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {
    private final ExpenseRepository expenseRepository;
    private final CategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;
    private final UserRepository userRepository;

    @Override
    public Mono<ExpenseResponseDto> createExpense(ExpenseRequestDto requestDto) {
        log.info("Creating new expense for userId:{}, categoryId:{}", requestDto.getUserId(), requestDto.getCategoryId());
        return Mono.fromCallable(() -> {
                    Category category = categoryRepository.findById(requestDto.getCategoryId())
                            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + requestDto.getCategoryId()));
                    User user = userRepository.findById(requestDto.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + requestDto.getUserId()));
                    Expense expense = expenseMapper.toEntity(requestDto, category, user);
                    Expense saved = expenseRepository.save(expense);
                    log.info("Expense created with id: {}", saved.getId());
                    return expenseMapper.toDto(saved);
                })
                .doOnError(e -> log.error("Error creating expense: {}", e.getMessage()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<ExpenseResponseDto> updateExpense(Long id, ExpenseRequestDto dto) {
        log.info("Updating expense with id: {}", id);
        return Mono.fromCallable(() -> {
                    Expense existingExpense = expenseRepository.findById(id)
                            .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + id));

                    Category category = categoryRepository.findById(dto.getCategoryId())
                            .orElseThrow(() -> new IllegalArgumentException("Category not found: " + dto.getCategoryId()));
                    User user = userRepository.findById(dto.getUserId())
                            .orElseThrow(() -> new IllegalArgumentException("User not found: " + dto.getUserId()));

                    existingExpense.setCategory(category);
                    existingExpense.setUser(user);
                    existingExpense.setAmount(dto.getAmount());
                    existingExpense.setDescription(dto.getDescription());
                    existingExpense.setDate(dto.getDate());

                    Expense updatedExpense = expenseRepository.save(existingExpense);
                    log.info("Expense updated with id: {}", updatedExpense.getId());
                    return expenseMapper.toDto(updatedExpense);
                })
                .doOnError(e -> log.error("Error updating expense with id {}: {}", id, e.getMessage()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<ExpenseResponseDto> getExpenseById(Long id) {
        log.info("Fetching expense with id: {}", id);
        return Mono.fromCallable(() ->
                        expenseRepository.findById(id)
                                .map(expenseMapper::toDto)
                                .orElseThrow(() -> new IllegalArgumentException("Expense not found: " + id))
                )
                .doOnError(e -> log.error("Error fetching expense with id {}: {}", id, e.getMessage()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<ExpenseResponseDto> getAllExpenses() {
        log.info("Fetching all expenses");
        return Mono.fromCallable(expenseRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(expenses -> {
                    log.info("Expenses found: {}", expenses.size());
                    return Flux.fromIterable(expenses)
                            .map(expenseMapper::toDto);
                })
                .doOnError(e -> log.error("Error fetching all expenses: {}", e.getMessage()));
    }

    @Override
    public Mono<Void> deleteExpense(Long id) {
        log.info("Deleting expense with id: {}", id);
        return Mono.fromCallable(() -> {
                    if (!expenseRepository.existsById(id)) {
                        throw new IllegalArgumentException("Expense not found: " + id);
                    }
                    expenseRepository.deleteById(id);
                    log.info("Expense deleted with id: {}", id);
                    return null;
                }).subscribeOn(Schedulers.boundedElastic())
                .doOnError(e -> log.error("Error deleting expense with id {}: {}", id, e.getMessage()))
                .then();
    }
}
