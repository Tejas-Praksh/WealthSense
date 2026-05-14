package com.wealthsense.common.exception;

import org.springframework.http.HttpStatus;

public class DuplicateResourceException extends WealthSenseException {

    public DuplicateResourceException(String resource, String identifier) {
        super(resource + " already exists with identifier: " + identifier,
                "DUPLICATE_RESOURCE",
                HttpStatus.CONFLICT);
    }
}
