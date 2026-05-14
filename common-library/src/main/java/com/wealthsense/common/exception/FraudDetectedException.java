package com.wealthsense.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.UUID;

@Getter
public class FraudDetectedException extends WealthSenseException {

    private final UUID transactionId;
    private final Double riskScore;

    public FraudDetectedException(UUID transactionId, Double riskScore, String reason) {
        super("Fraud detected for transaction " + transactionId + ": " + reason,
                "FRAUD_DETECTED",
                HttpStatus.FORBIDDEN);
        this.transactionId = transactionId;
        this.riskScore = riskScore;
    }
}
