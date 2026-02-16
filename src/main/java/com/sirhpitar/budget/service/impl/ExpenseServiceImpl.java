
package com.sirhpitar.budget.service.impl;

import com.sirhpitar.budget.dtos.request.ExpenseRequestDto;
import com.sirhpitar.budget.dtos.response.ExpenseResponseDto;
import com.sirhpitar.budget.entities.Expense;
import com.sirhpitar.budget.entities.ExpenseCategory;
import com.sirhpitar.budget.entities.User;
import com.sirhpitar.budget.exceptions.NotFoundException;
import com.sirhpitar.budget.mappers.ExpenseMapper;
import com.sirhpitar.budget.repository.ExpenseCategoryRepository;
import com.sirhpitar.budget.repository.ExpenseRepository;
import com.sirhpitar.budget.repository.UserRepository;
import com.sirhpitar.budget.service.ExpenseService;
import com.sirhpitar.budget.utils.ReactorBlocking;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCategoryRepository categoryRepository;
    private final ExpenseMapper expenseMapper;
    private final UserRepository userRepository;

    @Override
    public Mono<ExpenseResponseDto> createExpense(ExpenseRequestDto requestDto) {
        return ReactorBlocking.mono(() -> createExpenseInternal(requestDto));
    }

    @Override
    public Mono<ExpenseResponseDto> updateExpense(Long id, ExpenseRequestDto dto) {
        return ReactorBlocking.mono(() -> updateExpenseInternal(id, dto));
    }

    @Override
    public Mono<ExpenseResponseDto> getExpenseById(Long id) {
        return ReactorBlocking.mono(() -> getExpenseByIdInternal(id));
    }

    @Override
    public Flux<ExpenseResponseDto> getAllExpenses() {
        return ReactorBlocking.mono(expenseRepository::findAll)
                .flatMapMany(expenses -> Flux.fromIterable(expenses).map(expenseMapper::toDto));
    }

    @Override
    public Mono<Void> deleteExpense(Long id) {
        return ReactorBlocking.run(() -> deleteExpenseInternal(id));
    }

    // --- Private helper methods ---

    private ExpenseResponseDto createExpenseInternal(ExpenseRequestDto requestDto) {
        if (requestDto.getTransactionDate() == null) {
            requestDto.setTransactionDate(LocalDate.now());
        }

        ExpenseCategory category = getCategoryOrThrow(requestDto.getExpenseCategoryId());
        User user = getUserOrThrow(requestDto.getUserId());

        Expense expense = expenseMapper.toEntity(requestDto, category, user);
        Expense saved = expenseRepository.save(expense);

        return expenseMapper.toDto(saved);
    }

    private ExpenseResponseDto updateExpenseInternal(Long id, ExpenseRequestDto dto) {
        Expense existingExpense = getExpenseOrThrow(id);

        ExpenseCategory category = dto.getExpenseCategoryId() != null
                ? getCategoryOrThrow(dto.getExpenseCategoryId())
                : null;

        User user = dto.getUserId() != null
                ? getUserOrThrow(dto.getUserId())
                : null;

        expenseMapper.updateExpense(dto, existingExpense, category, user);

        Expense updated = expenseRepository.save(existingExpense);
        return expenseMapper.toDto(updated);
    }

    private ExpenseResponseDto getExpenseByIdInternal(Long id) {
        Expense expense = getExpenseOrThrow(id);
        return expenseMapper.toDto(expense);
    }

    private void deleteExpenseInternal(Long id) {
        if (!expenseRepository.existsById(id)) {
            throw new NotFoundException("Expense not found: " + id);
        }
        expenseRepository.deleteById(id);
    }

    private ExpenseCategory getCategoryOrThrow(Long categoryId) {
        return categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NotFoundException("Category not found: " + categoryId));
    }

    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));
    }

    private Expense getExpenseOrThrow(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Expense not found: " + id));
    }
}
