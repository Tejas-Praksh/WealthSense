package com.wealthsense.common.exception;

import org.springframework.http.HttpStatus;

public class RateLimitExceededException extends WealthSenseException {

    public RateLimitExceededException(String identifier) {
        super("Rate limit exceeded for: " + identifier,
                "RATE_LIMIT_EXCEEDED",
                HttpStatus.TOO_MANY_REQUESTS);
    }
}
