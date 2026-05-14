package com.wealthsense.common.testdata;

import com.wealthsense.common.enums.TransactionStatus;
import com.wealthsense.common.enums.TransactionType;
import com.wealthsense.common.events.TransactionEvent;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Shared factory for test payloads used across microservice tests (classpath-safe).
 * Prefer these helpers to keep fraud/transaction tests aligned on sample events.
 */
public final class TestDataBuilder {

    private TestDataBuilder() {
    }

    public static TransactionEvent transactionEvent(BigDecimal amount, String merchantName) {
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(UUID.randomUUID())
                .amount(amount)
                .type(TransactionType.DEBIT)
                .status(TransactionStatus.PENDING)
                .accountId(UUID.randomUUID())
                .merchantName(merchantName)
                .build();
        event.setUserId(UUID.randomUUID());
        event.setTimestamp(Instant.now());
        event.setCorrelationId(UUID.randomUUID().toString());
        return event;
    }

    public static TransactionEvent transactionEvent() {
        return transactionEvent(BigDecimal.valueOf(50_000), "Zomato");
    }
}
