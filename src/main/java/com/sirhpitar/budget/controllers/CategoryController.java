package com.sirhpitar.budget.controllers;

import com.sirhpitar.budget.apis.ApiResponse;
import com.sirhpitar.budget.apis.ApiResponseStatus;
import com.sirhpitar.budget.dtos.request.CategoryRequestDto;
import com.sirhpitar.budget.dtos.response.CategoryResponseDto;
import com.sirhpitar.budget.service.CategoryService;
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
    public Mono<ResponseEntity<ApiResponse<CategoryResponseDto>>> createCategory(CategoryRequestDto dto) {
        return categoryService.createCategory(dto)
                .map(data -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "Category created successfully", data)))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest().body(
                                new ApiResponse<>(ApiResponseStatus.ERROR, e.getMessage(), null)
                        )
                ));
    }

    @PutMapping("/update/{id}")
    public Mono<ResponseEntity<ApiResponse<CategoryResponseDto>>> updateCategory(
            @PathVariable Long id, @RequestBody CategoryRequestDto dto) {
        return categoryService.updateCategory(id, dto)
                .map(data -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "Category updated successfully", data)
                ))
                .onErrorResume(e -> {
                    String message = e.getMessage();
                    ApiResponseStatus status = ApiResponseStatus.ERROR;
                    if ("Category not found".equals(message)) {
                        status = ApiResponseStatus.NOT_FOUND;
                    }
                    return Mono.just(ResponseEntity.badRequest().body(
                            new ApiResponse<>(status, message, null)
                    ));
                });
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ApiResponse<CategoryResponseDto>>> getCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id)
                .map(data -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "Category fetched successfully", data)
                ))
                .switchIfEmpty(Mono.just(
                        ResponseEntity.status(404).body(
                                new ApiResponse<>(ApiResponseStatus.NOT_FOUND, "Category not found", null)
                        )
                ));
    }

    @GetMapping
    public Mono<ResponseEntity<ApiResponse<List<CategoryResponseDto>>>> getAllCategories() {
        return categoryService.getAllCategories()
                .collectList()
                .map(list -> ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "Categories fetched successfully", list)
                ));
    }

    @DeleteMapping("/delete/{id}")
    public Mono<ResponseEntity<ApiResponse<Void>>> deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id)
                .thenReturn(ResponseEntity.ok(
                        new ApiResponse<>(ApiResponseStatus.SUCCESS, "Category deleted successfully", (Void) null)
                ))
                .onErrorResume(e -> Mono.just(
                        ResponseEntity.badRequest().body(
                                new ApiResponse<>(ApiResponseStatus.ERROR, e.getMessage(), null)
                        )
                ));
    }
}

