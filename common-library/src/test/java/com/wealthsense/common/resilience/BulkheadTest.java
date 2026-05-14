package com.wealthsense.common.resilience;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadConfig;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * TDD tests for Bulkhead behaviour.
 *
 * <p>Uses an in-memory {@link BulkheadRegistry} — no Spring context needed.</p>
 */
class BulkheadTest {

    private static final int MAX_CONCURRENT = 5;

    private Bulkhead bulkhead;

    @BeforeEach
    void setUp() {
        BulkheadConfig config = BulkheadConfig.custom()
                .maxConcurrentCalls(MAX_CONCURRENT)
                .maxWaitDuration(Duration.ofMillis(0))  // reject immediately if full
                .build();

        bulkhead = BulkheadRegistry.of(config).bulkhead("testBulkhead");
    }

    @Test
    @DisplayName("Calls under the concurrency limit are allowed through")
    void bulkhead_underLimit_allowsCall() throws Exception {
        AtomicInteger successCount = new AtomicInteger(0);
        CountDownLatch insideLatch = new CountDownLatch(MAX_CONCURRENT);
        CountDownLatch releaseLatch = new CountDownLatch(1);

        ExecutorService executor = Executors.newFixedThreadPool(MAX_CONCURRENT);

        for (int i = 0; i < MAX_CONCURRENT; i++) {
            executor.submit(() -> {
                Bulkhead.decorateRunnable(bulkhead, () -> {
                    try {
                        insideLatch.countDown();
                        releaseLatch.await();
                        successCount.incrementAndGet();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }).run();
            });
        }

        insideLatch.await(); // all are inside bulkhead
        assertThat(bulkhead.getMetrics().getAvailableConcurrentCalls()).isEqualTo(0);

        releaseLatch.countDown(); // release all
        executor.shutdown();

        // Wait for completion
        Thread.sleep(200);
        assertThat(successCount.get())
                .as("All %d calls under the limit should succeed", MAX_CONCURRENT)
                .isEqualTo(MAX_CONCURRENT);
    }

    @Test
    @DisplayName("A call over the concurrency limit is rejected with BulkheadFullException")
    void bulkhead_overLimit_rejectsCall() {
        // Saturate the bulkhead: acquire all permits
        for (int i = 0; i < MAX_CONCURRENT; i++) {
            assertThat(bulkhead.tryAcquirePermission())
                    .as("First %d acquisitions should succeed", MAX_CONCURRENT)
                    .isTrue();
        }

        // Now the bulkhead is full — decorating another call should throw
        Supplier<String> overLimitSupplier = Bulkhead.decorateSupplier(
                bulkhead, () -> "should be rejected");

        assertThatThrownBy(overLimitSupplier::get)
                .isInstanceOf(BulkheadFullException.class);
    }

    @Test
    @DisplayName("After releasing a permit, calls are allowed again")
    void bulkhead_afterRelease_allowsCall() {
        bulkhead.tryAcquirePermission();
        bulkhead.onComplete(); // release

        Supplier<String> supplier = Bulkhead.decorateSupplier(bulkhead, () -> "ok");
        assertThat(supplier.get()).isEqualTo("ok");
    }
}
