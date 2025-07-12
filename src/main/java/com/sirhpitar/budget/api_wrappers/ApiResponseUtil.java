package com.sirhpitar.budget.api_wrappers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class ApiResponseUtil {

    // Private constructor to prevent instantiation
    private ApiResponseUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static <T> ResponseEntity<ApiResponse<T>> build(
            HttpStatus httpStatus,
            ApiResponseStatus apiStatus,
            String message,
            T data
    ) {
        Meta meta = new Meta(
                httpStatus.value(),
                apiStatus,
                message,
                LocalDateTime.now().toString()
        );
        return ResponseEntity.status(httpStatus).body(new ApiResponse<>(meta, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return build(HttpStatus.OK, ApiResponseStatus.SUCCESS, message, data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return build(status, ApiResponseStatus.ERROR, message, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return build(HttpStatus.NOT_FOUND, ApiResponseStatus.NOT_FOUND, message, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> validationError(String message) {
        return build(HttpStatus.BAD_REQUEST, ApiResponseStatus.VALIDATION_ERROR, message, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return build(HttpStatus.CREATED, ApiResponseStatus.SUCCESS, message, data);
    }
}
