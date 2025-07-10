package com.sirhpitar.budget.apis;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiResponse<T> {
    private ApiResponseStatus status;
    private String message;
    private T data;
}
