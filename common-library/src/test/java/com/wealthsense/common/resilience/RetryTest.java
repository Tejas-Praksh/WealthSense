package com.wealthsense.common.resilience;

import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.core.IntervalFunction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TDD tests for Retry behaviour.
 *
 * <p>All tests are pure in-memory — no Spring context, no network calls.</p>
 */
class RetryTest {

    private RetryRegistry registry;
    private Retry retry;

    @BeforeEach
    void setUp() {
        RetryConfig config = RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(10))   // short for tests
                .build();

        registry = RetryRegistry.of(config);
        retry = registry.retry("testRetry");
    }

    @Test
    @DisplayName("On first failure, retry attempts the call again (total 3 attempts)")
    void retry_firstFailure_retriesAgain() {
        AtomicInteger callCount = new AtomicInteger(0);

        Supplier<String> supplier = Retry.decorateSupplier(retry, () -> {
            callCount.incrementAndGet();
            throw new RuntimeException("transient error");
        });

        // All 3 attempts fail → exception eventually propagated
        try { supplier.get(); } catch (Exception ignored) { }

        assertThat(callCount.get())
                .as("Should have attempted exactly 3 times (maxAttempts=3)")
                .isEqualTo(3);
    }

    @Test
    @DisplayName("After maxAttempts=3 failures, the original exception is propagated")
    void retry_3failures_throwsException() {
        Supplier<String> supplier = Retry.decorateSupplier(retry, () -> {
            throw new RuntimeException("permanent failure");
        });

        assertThatThrownBy(supplier::get)
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("permanent failure");
    }

    @Test
    @DisplayName("Retry succeeds on the second attempt — returns correct value")
    void retry_successOnSecondAttempt_returnsValue() {
        AtomicInteger callCount = new AtomicInteger(0);

        Supplier<String> supplier = Retry.decorateSupplier(retry, () -> {
            if (callCount.incrementAndGet() < 2) {
                throw new RuntimeException("transient");
            }
            return "success";
        });

        String result = supplier.get();

        assertThat(result).isEqualTo("success");
        assertThat(callCount.get()).isEqualTo(2);
    }

    @Test
    @DisplayName("Exponential-backoff retry configuration increases wait intervals")
    void retry_exponentialBackoff_increasesDelay() {
        RetryConfig exponentialConfig = RetryConfig.custom()
                .maxAttempts(3)
                .intervalFunction(IntervalFunction.ofExponentialBackoff(100L, 2.0))
                .build();

        Retry exponentialRetry = RetryRegistry.of(exponentialConfig)
                .retry("exponentialTest");

        AtomicInteger call = new AtomicInteger(0);
        long start = System.currentTimeMillis();

        Supplier<String> supplier = Retry.decorateSupplier(exponentialRetry, () -> {
            call.incrementAndGet();
            throw new RuntimeException("fail");
        });

        try { supplier.get(); } catch (Exception ignored) { }

        long elapsed = System.currentTimeMillis() - start;

        // With 100ms base and multiplier=2: wait1=100ms, wait2=200ms → ≥ 300ms total
        assertThat(elapsed)
                .as("Exponential backoff should wait at least 300 ms across 3 attempts")
                .isGreaterThanOrEqualTo(300L);
        assertThat(call.get()).isEqualTo(3);
    }
}
