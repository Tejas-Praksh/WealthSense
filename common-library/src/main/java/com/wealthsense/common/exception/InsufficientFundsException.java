package com.wealthsense.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
public class InsufficientFundsException extends WealthSenseException {

    private final UUID accountId;
    private final BigDecimal required;
    private final BigDecimal available;

    public InsufficientFundsException(UUID accountId, BigDecimal required,
                                      BigDecimal available) {
        super("Insufficient funds. Required: " + required
                        + " paise, Available: " + available + " paise",
                "INSUFFICIENT_FUNDS",
                HttpStatus.UNPROCESSABLE_ENTITY);
        this.accountId = accountId;
        this.required = required;
        this.available = available;
    }
}
