package com.wealthsense.common.exception;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExceptionTest {

    @Test
    void resourceNotFoundException_correctMessage() {
        ResourceNotFoundException ex =
                new ResourceNotFoundException("User", "123");

        assertTrue(ex.getMessage().contains("User"));
        assertTrue(ex.getMessage().contains("123"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getHttpStatus());
        assertEquals("RESOURCE_NOT_FOUND", ex.getErrorCode());
    }

    @Test
    void validationException_correctFields() {
        ValidationException ex =
                new ValidationException("Invalid email format");

        assertEquals("Invalid email format", ex.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, ex.getHttpStatus());
        assertEquals("VALIDATION_FAILED", ex.getErrorCode());
    }

    @Test
    void duplicateResourceException_correctFields() {
        DuplicateResourceException ex =
                new DuplicateResourceException("Account", "ACC-001");

        assertTrue(ex.getMessage().contains("Account"));
        assertTrue(ex.getMessage().contains("ACC-001"));
        assertEquals(HttpStatus.CONFLICT, ex.getHttpStatus());
        assertEquals("DUPLICATE_RESOURCE", ex.getErrorCode());
    }

    @Test
    void unauthorizedException_correctFields() {
        UnauthorizedException ex =
                new UnauthorizedException("Token expired");

        assertEquals("Token expired", ex.getMessage());
        assertEquals(HttpStatus.UNAUTHORIZED, ex.getHttpStatus());
        assertEquals("UNAUTHORIZED", ex.getErrorCode());
    }

    @Test
    void fraudDetectedException_correctFields() {
        UUID txnId = UUID.randomUUID();
        FraudDetectedException ex =
                new FraudDetectedException(txnId, 0.95, "Suspicious pattern");

        assertEquals(txnId, ex.getTransactionId());
        assertEquals(0.95, ex.getRiskScore());
        assertEquals(HttpStatus.FORBIDDEN, ex.getHttpStatus());
        assertEquals("FRAUD_DETECTED", ex.getErrorCode());
    }

    @Test
    void insufficientFundsException_correctFields() {
        UUID accountId = UUID.randomUUID();
        BigDecimal required = BigDecimal.valueOf(5000);
        BigDecimal available = BigDecimal.valueOf(2000);

        InsufficientFundsException ex =
                new InsufficientFundsException(accountId, required, available);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, ex.getHttpStatus());
        assertEquals("INSUFFICIENT_FUNDS", ex.getErrorCode());
        assertEquals(accountId, ex.getAccountId());
        assertEquals(0, ex.getRequired().compareTo(BigDecimal.valueOf(5000)));
        assertEquals(0, ex.getAvailable().compareTo(BigDecimal.valueOf(2000)));
        assertTrue(ex.getMessage().contains("Insufficient funds"));
    }

    @Test
    void rateLimitExceededException_correctFields() {
        RateLimitExceededException ex =
                new RateLimitExceededException("user-123");

        assertTrue(ex.getMessage().contains("user-123"));
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, ex.getHttpStatus());
        assertEquals("RATE_LIMIT_EXCEEDED", ex.getErrorCode());
    }

    @Test
    void wealthSenseException_withCause() {
        RuntimeException cause = new RuntimeException("Original error");
        WealthSenseException ex = new WealthSenseException(
                "Wrapped error", "INTERNAL_ERROR",
                HttpStatus.INTERNAL_SERVER_ERROR, cause);

        assertEquals(cause, ex.getCause());
        assertEquals("Wrapped error", ex.getMessage());
    }
}
