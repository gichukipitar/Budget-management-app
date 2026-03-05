package com.sirhpitar.budget.api_wrappers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class ApiResponseUtil {

    private ApiResponseUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    public static <T> ResponseEntity<ApiResponse<T>> build(
            HttpStatus httpStatus,
            ApiResponseStatus apiStatus,
            String message,
            String requestId,
            String errorCode,
            T data
    ) {
        Meta meta = new Meta(
                httpStatus.value(),
                apiStatus,
                message,
                LocalDateTime.now().toString(),
                requestId,
                errorCode
        );

        return ResponseEntity.status(httpStatus).body(new ApiResponse<>(meta, data));
    }

    public static <T> ResponseEntity<ApiResponse<T>> build(
            HttpStatus httpStatus,
            ApiResponseStatus apiStatus,
            String message,
            T data
    ) {
        return build(httpStatus, apiStatus, message, null, null, data);
    }

    //helpers
    public static <T> ResponseEntity<ApiResponse<T>> success(String message, T data) {
        return build(HttpStatus.OK, ApiResponseStatus.SUCCESS, message, null, null, data);
    }

    public static ResponseEntity<ApiResponse<Void>> successVoid(String message) {
        return success(message, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> created(String message, T data) {
        return build(HttpStatus.CREATED, ApiResponseStatus.SUCCESS, message, null, null, data);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(HttpStatus status, String message) {
        return build(status, ApiResponseStatus.ERROR, message, null, null, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> error(
            HttpStatus status,
            ApiResponseStatus apiStatus,
            String message,
            String requestId,
            String errorCode
    ) {
        return build(status, apiStatus, message, requestId, errorCode, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> notFound(String message) {
        return build(HttpStatus.NOT_FOUND, ApiResponseStatus.NOT_FOUND, message, null, null, null);
    }

    public static <T> ResponseEntity<ApiResponse<T>> validationError(String message) {
        return build(HttpStatus.BAD_REQUEST, ApiResponseStatus.VALIDATION_ERROR, message, null, null, null);
    }
}