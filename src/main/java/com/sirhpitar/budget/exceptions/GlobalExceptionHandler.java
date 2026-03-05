package com.sirhpitar.budget.exceptions;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseStatus;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
import com.sirhpitar.budget.config.RequestIdConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.resource.NoResourceFoundException;
import org.springframework.web.server.ServerWebExchange;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {


    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(
            WebExchangeBindException ex,
            ServerWebExchange exchange
    ) {
        String requestId = RequestIdConfig.getRequestId(exchange);

        String message = ex.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .distinct()
                .reduce((m1, m2) -> m1 + "; " + m2)
                .orElse("Validation error");

        return ApiResponseUtil.error(
                HttpStatus.BAD_REQUEST,
                ApiResponseStatus.VALIDATION_ERROR,
                message,
                requestId,
                "VALIDATION_ERROR"
        );
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(
            ApiException ex,
            ServerWebExchange exchange
    ) {
        String requestId = RequestIdConfig.getRequestId(exchange);

        if (ex.getHttpStatus().is5xxServerError()) {
            log.error("ApiException [{}] requestId={} message={}", ex.getHttpStatus(), requestId, ex.getMessage(), ex);
        } else {
            log.warn("ApiException [{}] requestId={} message={}", ex.getHttpStatus(), requestId, ex.getMessage());
        }

        ApiResponseStatus apiStatus = mapStatus(ex.getHttpStatus());

        return ApiResponseUtil.error(
                ex.getHttpStatus(),
                apiStatus,
                ex.getMessage(),
                requestId,
                ex.getErrorCode()
        );
    }


    @ExceptionHandler({DataIntegrityViolationException.class, DuplicateKeyException.class})
    public ResponseEntity<ApiResponse<Void>> handleJdbcDataIntegrityViolation(
            Exception ex,
            ServerWebExchange exchange
    ) {
        String requestId = RequestIdConfig.getRequestId(exchange);

        log.error("Data integrity violation requestId={}", requestId, ex);

        String cleanedMessage = parsePostgresDuplicateKeyMessage(ex.getMessage());

        return ApiResponseUtil.error(
                HttpStatus.BAD_REQUEST,
                ApiResponseStatus.BAD_REQUEST,
                cleanedMessage,
                requestId,
                "DATA_INTEGRITY_VIOLATION"
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoResourceFound(
            NoResourceFoundException ex,
            ServerWebExchange exchange
    ) {
        String requestId = RequestIdConfig.getRequestId(exchange);
        return ApiResponseUtil.error(
                HttpStatus.NOT_FOUND,
                ApiResponseStatus.NOT_FOUND,
                ex.getMessage(),
                requestId,
                "NOT_FOUND"
        );
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(
            IllegalArgumentException ex,
            ServerWebExchange exchange
    ) {
        String requestId = RequestIdConfig.getRequestId(exchange);
        return ApiResponseUtil.error(
                HttpStatus.BAD_REQUEST,
                ApiResponseStatus.BAD_REQUEST,
                ex.getMessage(),
                requestId,
                "BAD_REQUEST"
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(
            Exception ex,
            ServerWebExchange exchange
    ) {
        String requestId = RequestIdConfig.getRequestId(exchange);
        log.error("Unhandled exception requestId={}", requestId, ex);

        return ApiResponseUtil.error(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ApiResponseStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred.",
                requestId,
                "INTERNAL_SERVER_ERROR"
        );
    }

    private ApiResponseStatus mapStatus(HttpStatus status) {
        if (status == HttpStatus.UNAUTHORIZED) return ApiResponseStatus.UNAUTHORIZED;
        if (status == HttpStatus.FORBIDDEN) return ApiResponseStatus.FORBIDDEN;
        if (status == HttpStatus.NOT_FOUND) return ApiResponseStatus.NOT_FOUND;
        if (status == HttpStatus.BAD_REQUEST) return ApiResponseStatus.BAD_REQUEST;
        if (status == HttpStatus.TOO_MANY_REQUESTS) return ApiResponseStatus.ERROR;
        if (status.is5xxServerError()) return ApiResponseStatus.INTERNAL_SERVER_ERROR;
        return ApiResponseStatus.ERROR;
    }

    private String parsePostgresDuplicateKeyMessage(String rawMessage) {
        if (rawMessage == null) return "Duplicate value detected.";

        if (rawMessage.contains("uk_expense_user_category_amount_date")) {
            return "An expense with the same category, amount, and date already exists for this user.";
        }
        if (rawMessage.contains("uk_expense_receipt_url")) {
            return "This receipt URL is already associated with another expense.";
        }

        Pattern pattern = Pattern.compile("Key \\((\\w+)\\)=\\([^)]+\\) already exists");
        Matcher matcher = pattern.matcher(rawMessage);
        if (matcher.find()) {
            return String.format("The %s you provided is already in use.", matcher.group(1));
        }

        Pattern constraintPattern = Pattern.compile("violates unique constraint \"(\\w+)\"");
        Matcher constraintMatcher = constraintPattern.matcher(rawMessage);
        if (constraintMatcher.find()) {
            String field = guessFieldFromConstraint(constraintMatcher.group(1));
            return String.format("The %s you provided is already in use.", field);
        }

        return "Duplicate value detected.";
    }

    private String guessFieldFromConstraint(String constraint) {
        Pattern fieldPattern = Pattern.compile("(?i)(email|username|category|budget|phone|expense|receipt)");
        Matcher matcher = fieldPattern.matcher(constraint);
        if (matcher.find()) return matcher.group(1);
        return "value";
    }
}