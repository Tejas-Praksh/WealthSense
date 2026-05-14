package com.wealthsense.security.idempotency;

import java.time.Instant;

public class IdempotencyResult<T> {

    private final boolean duplicate;
    private final T result;
    private final Instant processedAt;

    private IdempotencyResult(boolean duplicate, T result, Instant processedAt) {
        this.duplicate = duplicate;
        this.result = result;
        this.processedAt = processedAt;
    }

    public static <T> IdempotencyResult<T> fresh(T result, Instant processedAt) {
        return new IdempotencyResult<>(false, result, processedAt);
    }

    public static <T> IdempotencyResult<T> duplicate(T result, Instant processedAt) {
        return new IdempotencyResult<>(true, result, processedAt);
    }

    public boolean isDuplicate() {
        return duplicate;
    }

    public T getResult() {
        return result;
    }

    public Instant getProcessedAt() {
        return processedAt;
    }
}

