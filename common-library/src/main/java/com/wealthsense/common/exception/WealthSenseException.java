package com.wealthsense.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class WealthSenseException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;

    public WealthSenseException(String message, String errorCode, HttpStatus httpStatus) {
        super(message);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }

    public WealthSenseException(String message, String errorCode,
                                HttpStatus httpStatus, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
    }
}
