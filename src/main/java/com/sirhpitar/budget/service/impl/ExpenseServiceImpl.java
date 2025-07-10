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
        return Mono.fromCallable(() -> {
            Category category = categoryRepository.findById(requestDto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            User user = userRepository.findById(requestDto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            Expense expense = expenseMapper.toEntity(requestDto, category, user);
            Expense saved = expenseRepository.save(expense);
            return expenseMapper.toDto(saved);
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<ExpenseResponseDto> updateExpense(Long id, ExpenseRequestDto dto) {
        return Mono.fromCallable(() -> {
            //find existing expense
            Expense existingExpense = expenseRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Expense not found"));
            //find related category and user
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));

            //update fields
            existingExpense.setCategory(category);
            existingExpense.setUser(user);
            existingExpense.setAmount(dto.getAmount());
            existingExpense.setDescription(dto.getDescription());
            existingExpense.setDate(dto.getDate());

            //save updated expense
            Expense updatedExpense = expenseRepository.save(existingExpense);
            return expenseMapper.toDto(updatedExpense);

        }).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<ExpenseResponseDto> getExpenseById(Long id) {
        return Mono.fromCallable(() ->
                expenseRepository.findById(id)
                        .map(expenseMapper::toDto)
                        .orElseThrow(() -> new IllegalArgumentException("Expense not found"))

        ).subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Flux<ExpenseResponseDto> getAllExpenses() {
        return Mono.fromCallable(expenseRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(expenses -> Flux.fromIterable(expenses)
                        .map(expenseMapper::toDto));
    }

    @Override
    public Mono<Void> deleteExpense(Long id) {
        return Mono.fromCallable(() -> {
                    if (!expenseRepository.existsById(id)) {
                        throw new IllegalArgumentException("Expense not found");
                    }
                    expenseRepository.deleteById(id);
                    return null;
                }).subscribeOn(Schedulers.boundedElastic())
                .then();
    }
}
