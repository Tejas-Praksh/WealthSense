package com.wealthsense.decision.saga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ValidateBalanceStepTest {

    private ValidateBalanceStep step;

    @BeforeEach
    void setUp() {
        step = new ValidateBalanceStep();
    }

    @Test
    void execute_sufficientBalance_returns_success() {
        SagaContext context = SagaContext.builder()
                .transactionId(UUID.randomUUID())
                .amount(new BigDecimal("100.00"))
                .build();

        SagaStepResult result = step.execute(context);

        assertTrue(result.isSuccess());
        assertEquals("BalanceValidated", context.getResult("ValidateBalance"));
    }

    @Test
    void execute_insufficientBalance_returns_failure() {
        SagaContext context = SagaContext.builder()
                .transactionId(UUID.randomUUID())
                .amount(new BigDecimal("-50.00"))
                .build();

        SagaStepResult result = step.execute(context);

        assertFalse(result.isSuccess());
        assertEquals("Invalid amount detected", result.getErrorMessage());
    }

    @Test
    void compensate_doesNothing() {
        SagaContext context = SagaContext.builder().build();
        assertDoesNotThrow(() -> step.compensate(context));
    }
}
