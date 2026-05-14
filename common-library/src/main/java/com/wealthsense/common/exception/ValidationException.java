package com.wealthsense.common.exception;

import org.springframework.http.HttpStatus;

public class ValidationException extends WealthSenseException {

    public ValidationException(String message) {
        super(message, "VALIDATION_FAILED", HttpStatus.BAD_REQUEST);
    }
}
