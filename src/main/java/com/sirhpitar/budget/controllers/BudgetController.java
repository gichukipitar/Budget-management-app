package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseStatus;
import com.sirhpitar.budget.dtos.request.BudgetRequestDto;
import com.sirhpitar.budget.dtos.response.BudgetResponseDto;
import com.sirhpitar.budget.service.BudgetService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetController {
    private final BudgetService budgetService;

    @PostMapping("/create")
    public Mono<ResponseEntity<ApiResponse<BudgetResponseDto>>> createBudget(@RequestBody BudgetRequestDto dto) {
        return budgetService.createBudget(dto)
                .map(data -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "Budget created successfully", data)))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest().body(
                                new ApiResponse<>(ApiResponseStatus.ERROR, e.getMessage(), null))
                ));
    }

    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<ApiResponse<BudgetResponseDto>>> updateBudget(@PathVariable Long id,
                                                                             @RequestBody BudgetRequestDto dto) {
        return budgetService.updateBudget(id, dto)
                .map(data -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "Budget updated successfully", data)))
                .onErrorResume(e -> {
                    String message = e.getMessage();
                    ApiResponseStatus status = ApiResponseStatus.ERROR;
                    if ("Budget not found".equals(message)) {
                        status = ApiResponseStatus.NOT_FOUND;
                    }
                    return Mono.just(ResponseEntity.badRequest().body(
                            new ApiResponse<>(status, message, null)
                    ));
                });

    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<BudgetResponseDto>>> getBudget(@PathVariable Long id) {
        return budgetService.getBudgetById(id)
                .map(data -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "Budget fetched successfully", data)))
                .switchIfEmpty(Mono.just(
                        ResponseEntity.status(404).body(
                                new ApiResponse<>(ApiResponseStatus.NOT_FOUND, "Budget not found", null))
                ));
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<ApiResponse<List<BudgetResponseDto>>>> getAllBudgets() {
        return budgetService.getAllBudgets()
                .collectList()
                .map(list -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "All budgets fetched successfully", list)
                ));
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteBudget(@PathVariable Long id) {
        return budgetService.deleteBudget(id)
                .then(Mono.just(ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "Budget deleted successfully", (Void) null))))
                .onErrorResume(e -> Mono.just(ResponseEntity.badRequest().body(
                        new ApiResponse<>(ApiResponseStatus.ERROR, e.getMessage(), null)
                )));
    }

}