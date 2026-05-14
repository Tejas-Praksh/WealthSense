package com.wealthsense.common.resilience;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TDD tests for Circuit Breaker behaviour.
 *
 * <p>Uses Resilience4J in-memory registry — no Spring context needed,
 * so these are fast unit tests that run without any infrastructure.</p>
 */
class CircuitBreakerTest {

    private CircuitBreakerRegistry registry;
    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowType(CircuitBreakerConfig.SlidingWindowType.COUNT_BASED)
                .slidingWindowSize(10)
                .failureRateThreshold(50)                        // 5/10 = 50 % → OPEN
                .waitDurationInOpenState(Duration.ofSeconds(10))
                .permittedNumberOfCallsInHalfOpenState(3)
                .slowCallDurationThreshold(Duration.ofSeconds(2))
                .slowCallRateThreshold(50)
                .build();

        registry = CircuitBreakerRegistry.of(config);
        circuitBreaker = registry.circuitBreaker("testService");
    }

    // ────────────────────────────── tests ────────────────────────────────────

    @Test
    @DisplayName("5 failures in a sliding window of 10 transitions the circuit to OPEN")
    void circuitBreaker_5failures_opensCircuit() {
        // Given – need ≥ 5 failures out of 10 calls to hit 50 % threshold
        Supplier<String> failingSupplier =
                CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
                    throw new RuntimeException("downstream failure");
                });

        // When – call 10 times, each time it fails (50 % failure rate = 100 %)
        for (int i = 0; i < 10; i++) {
            try { failingSupplier.get(); } catch (Exception ignored) { }
        }

        // Then
        assertThat(circuitBreaker.getState())
                .as("Circuit should be OPEN after 10 consecutive failures (100% > 50% threshold)")
                .isEqualTo(CircuitBreaker.State.OPEN);
    }

    @Test
    @DisplayName("A call in OPEN state throws CallNotPermittedException (no network call made)")
    void circuitBreaker_openState_returnsFallback() {
        // Force circuit into OPEN by exhausting failures
        forceClosed_to_Open();

        Supplier<String> supplier =
                CircuitBreaker.decorateSupplier(circuitBreaker,
                        () -> "should never reach here");

        // When / Then
        assertThatThrownBy(supplier::get)
                .isInstanceOf(CallNotPermittedException.class)
                .hasMessageContaining("CircuitBreaker");
    }

    @Test
    @DisplayName("After waitDurationInOpenState the circuit transitions to HALF_OPEN and allows test calls")
    void circuitBreaker_halfOpen_allowsTestRequest() {
        forceClosed_to_Open();

        // Transition to HALF_OPEN manually (normally time-based)
        circuitBreaker.transitionToHalfOpenState();

        assertThat(circuitBreaker.getState())
                .isEqualTo(CircuitBreaker.State.HALF_OPEN);

        // HALF_OPEN allows permittedNumberOfCallsInHalfOpenState calls
        // A successful call should be permitted
        Supplier<String> successSupplier =
                CircuitBreaker.decorateSupplier(circuitBreaker, () -> "ok");
        assertThat(successSupplier.get()).isEqualTo("ok");
    }

    @Test
    @DisplayName("Successful calls in HALF_OPEN state close the circuit")
    void circuitBreaker_successInHalfOpen_closes() {
        forceClosed_to_Open();
        circuitBreaker.transitionToHalfOpenState();

        Supplier<String> successSupplier =
                CircuitBreaker.decorateSupplier(circuitBreaker, () -> "ok");

        // Execute all permitted calls successfully
        for (int i = 0; i < 3; i++) {
            successSupplier.get();
        }

        assertThat(circuitBreaker.getState())
                .as("Circuit should CLOSE after all half-open test calls succeed")
                .isEqualTo(CircuitBreaker.State.CLOSED);
    }

    // ──────────────────────────── helpers ─────────────────────────────────────

    /** Force the circuit from CLOSED → OPEN by making 10 failing calls. */
    private void forceClosed_to_Open() {
        Supplier<String> failing =
                CircuitBreaker.decorateSupplier(circuitBreaker, () -> {
                    throw new RuntimeException("forced failure");
                });
        for (int i = 0; i < 10; i++) {
            try { failing.get(); } catch (Exception ignored) { }
        }
    }
}
