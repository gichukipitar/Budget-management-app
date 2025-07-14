package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
import com.sirhpitar.budget.dtos.request.BudgetCategoryRequestDto;
import com.sirhpitar.budget.dtos.response.BudgetCategoryResponseDto;
import com.sirhpitar.budget.service.BudgetCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
@Slf4j
public class BudgetCategoryController {
    private final BudgetCategoryService categoryService;

    @PostMapping("/create")
    public Mono<ResponseEntity<ApiResponse<BudgetCategoryResponseDto>>> createCategory(@Valid @RequestBody BudgetCategoryRequestDto dto) {
        return categoryService.createCategory(dto)
                .map(data -> ApiResponseUtil.success("Category created successfully", data));
    }

    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<ApiResponse<BudgetCategoryResponseDto>>> updateCategory(
            @PathVariable Long id, @Valid @RequestBody BudgetCategoryRequestDto dto) {
        return categoryService.updateCategory(id, dto)
                .map(data -> ApiResponseUtil.success("Category updated successfully", data));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<BudgetCategoryResponseDto>>> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(data -> ApiResponseUtil.success("Category fetched successfully", data));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<BudgetCategoryResponseDto>>>> getAllCategories() {
        return categoryService.getAllCategories()
                .collectList()
                .map(list -> ApiResponseUtil.success("Categories fetched successfully", list));
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id)
                .then(Mono.just(ApiResponseUtil.success("Category deleted successfully", null)));
    }
}