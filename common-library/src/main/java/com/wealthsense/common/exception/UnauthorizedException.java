package com.wealthsense.common.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends WealthSenseException {

    public UnauthorizedException(String message) {
        super(message, "UNAUTHORIZED", HttpStatus.UNAUTHORIZED);
    }
}
