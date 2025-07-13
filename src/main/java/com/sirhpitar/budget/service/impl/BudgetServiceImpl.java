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
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetServiceImpl implements BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final BudgetMapper budgetMapper;

    public Mono<BudgetResponseDto> createBudget(BudgetRequestDto dto) {
        return Mono.fromCallable(() -> userRepository.findById(dto.getUserId()))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalUser -> {
                    if (optionalUser.isEmpty()) {
                        return Mono.error(new NotFoundException("User not found"));
                    }
                    User user = optionalUser.get();

                    if (dto.getBudgetDate() == null) {
                        dto.setBudgetDate(LocalDate.now());
                    }

                    Budget budget = budgetMapper.toEntity(dto, user);

                    return Mono.fromCallable(() -> budgetRepository.save(budget))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(budgetMapper::toDto);
                });
    }

    @Override
    public Mono<BudgetResponseDto> getBudgetById(Long id) {
        return Mono.fromCallable(() -> budgetRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalBudget -> optionalBudget
                        .map(budget -> Mono.just(budgetMapper.toDto(budget)))
                        .orElseGet(Mono::empty)
                );
    }

    @Override
    public Mono<BudgetResponseDto> updateBudget(Long id, BudgetRequestDto dto) {
        return Mono.fromCallable(() -> budgetRepository.findById(id))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(optionalBudget -> {
                    if (optionalBudget.isEmpty()) {
                        return Mono.error(new NotFoundException("Budget not found"));
                    }

                    Budget budget = optionalBudget.get();

                    LocalDate dateToSet = dto.getBudgetDate() != null ? dto.getBudgetDate() : LocalDate.now();
                    budget.setBudgetDate(dateToSet);

                    if (dto.getUserId() != null && !dto.getUserId().equals(budget.getUser().getId())) {
                        return Mono.fromCallable(() -> userRepository.findById(dto.getUserId()))
                                .subscribeOn(Schedulers.boundedElastic())
                                .flatMap(optionalUser -> {
                                    if (optionalUser.isEmpty()) {
                                        return Mono.error(new NotFoundException("User not found"));
                                    }
                                    budget.setUser(optionalUser.get());
                                    return Mono.fromCallable(() -> budgetRepository.save(budget))
                                            .subscribeOn(Schedulers.boundedElastic())
                                            .map(budgetMapper::toDto);
                                });
                    }
                    return Mono.fromCallable(() -> budgetRepository.save(budget))
                            .subscribeOn(Schedulers.boundedElastic())
                            .map(budgetMapper::toDto);

                });
    }

    @Override
    public Mono<Void> deleteBudget(Long id) {
        return Mono.fromCallable(() -> {
            if (!budgetRepository.existsById(id)) {
                throw new NotFoundException("Budget not found");
            }
            budgetRepository.deleteById(id);
            return null;
        }).subscribeOn(Schedulers.boundedElastic()).then();
    }

    @Override
    public Flux<BudgetResponseDto> getAllBudgets() {
        return Mono.fromCallable(budgetRepository::findAll)
                .subscribeOn(Schedulers.boundedElastic())
                .flatMapMany(budgets -> Flux.fromIterable(budgets)
                        .map(budgetMapper::toDto));
    }
}
