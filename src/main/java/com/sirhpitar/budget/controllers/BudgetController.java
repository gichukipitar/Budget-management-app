package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
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
                .map(data -> ApiResponseUtil.success("Budget created successfully", data));
    }

    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<ApiResponse<BudgetResponseDto>>> updateBudget(@PathVariable Long id,
                                                                             @RequestBody BudgetRequestDto dto) {
        return budgetService.updateBudget(id, dto)
                .map(data -> ApiResponseUtil.success("Budget updated successfully", data));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<BudgetResponseDto>>> getBudget(@PathVariable Long id) {
        return budgetService.getBudgetById(id)
                .map(data -> ApiResponseUtil.success("Budget fetched successfully", data));
    }

    @GetMapping("/all")
    public Mono<ResponseEntity<ApiResponse<List<BudgetResponseDto>>>> getAllBudgets() {
        return budgetService.getAllBudgets()
                .collectList()
                .map(list -> ApiResponseUtil.success("All budgets fetched successfully", list));
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteBudget(@PathVariable Long id) {
        return budgetService.deleteBudget(id)
                .then(Mono.just(ApiResponseUtil.success("Budget deleted successfully", null)));
    }
}