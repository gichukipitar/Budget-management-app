package com.sirhpitar.budget.exceptions;

import com.sirhpitar.budget.api_wrappers.ApiResponse;
import com.sirhpitar.budget.api_wrappers.ApiResponseUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(WebExchangeBindException ex) {
        String message = ex.getAllErrors().stream()
                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                .reduce((m1, m2) -> m1 + "; " + m2)
                .orElse("Validation error");
        return ApiResponseUtil.validationError(message);
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleNotFoundException(NotFoundException ex) {
        return ApiResponseUtil.notFound(ex.getMessage());
    }

    @ExceptionHandler({DataIntegrityViolationException.class, DuplicateKeyException.class})
    public ResponseEntity<ApiResponse<Void>> handleJdbcDataIntegrityViolation(Exception ex) {
        log.error("Data integrity violation:", ex);
        String rawMessage = ex.getMessage();
        String cleanedMessage = parsePostgresDuplicateKeyMessage(rawMessage);
        return ApiResponseUtil.error(HttpStatus.BAD_REQUEST, cleanedMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return ApiResponseUtil.error(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
        log.error("Unhandled exception caught in global handler", ex);
        return ApiResponseUtil.error(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.");
    }

    private String parsePostgresDuplicateKeyMessage(String rawMessage) {
        if (rawMessage == null) return "Duplicate value detected.";
        Pattern pattern = Pattern.compile("Key \\((\\w+)\\)=\\([^)]+\\) already exists");
        Matcher matcher = pattern.matcher(rawMessage);
        if (matcher.find()) {
            String field = matcher.group(1);
            return String.format("The %s you provided is already in use.", field);
        }
        Pattern constraintPattern = Pattern.compile("violates unique constraint \"(\\w+)\"");
        Matcher constraintMatcher = constraintPattern.matcher(rawMessage);
        if (constraintMatcher.find()) {
            String constraint = constraintMatcher.group(1);
            String field = guessFieldFromConstraint(constraint);
            return String.format("The %s you provided is already in use.", field);
        }
        return "Duplicate value detected.";
    }

    private String guessFieldFromConstraint(String constraint) {
        Pattern fieldPattern = Pattern.compile("(?i)(email|username|category|budget|phone)");
        Matcher matcher = fieldPattern.matcher(constraint);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "value";
    }
}