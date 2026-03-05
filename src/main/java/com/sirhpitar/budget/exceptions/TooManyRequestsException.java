package com.sirhpitar.budget.exceptions;

import org.springframework.http.HttpStatus;

public class TooManyRequestsException extends ApiException {
    public TooManyRequestsException(String message) {
        super(HttpStatus.TOO_MANY_REQUESTS, message, "TOO_MANY_REQUESTS");
    }
}