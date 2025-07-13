package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
import com.sirhpitar.budget.dtos.request.CategoryRequestDto;
import com.sirhpitar.budget.dtos.response.CategoryResponseDto;
import com.sirhpitar.budget.service.CategoryService;
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
public class CategoryController {
    private final CategoryService categoryService;

    @PostMapping("/create")
    public Mono<ResponseEntity<ApiResponse<CategoryResponseDto>>> createCategory(@Valid @RequestBody CategoryRequestDto dto) {
        return categoryService.createCategory(dto)
                .map(data -> ApiResponseUtil.success("Category created successfully", data));
    }

    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<ApiResponse<CategoryResponseDto>>> updateCategory(
            @PathVariable Long id, @Valid @RequestBody CategoryRequestDto dto) {
        return categoryService.updateCategory(id, dto)
                .map(data -> ApiResponseUtil.success("Category updated successfully", data));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<CategoryResponseDto>>> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(data -> ApiResponseUtil.success("Category fetched successfully", data));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<CategoryResponseDto>>>> getAllCategories() {
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