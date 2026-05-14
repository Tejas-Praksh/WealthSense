package com.wealthsense.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * AOP aspect that intercepts methods annotated with Resilience4J annotations
 * and publishes detailed metrics to Micrometer / Prometheus.
 *
 * <p>Metrics published:
 * <ul>
 *   <li>{@code resilience.call.success} (counter, tags: service, method)</li>
 *   <li>{@code resilience.call.failure} (counter, tags: service, method, reason)</li>
 *   <li>{@code resilience.call.fallback} (counter, tags: service, reason)</li>
 *   <li>{@code resilience.call.duration} (timer, tags: service, outcome)</li>
 * </ul>
 * </p>
 *
 * <p>Also logs circuit-state changes and fallback activations at WARN level
 * with the current correlationId from MDC.</p>
 */
@Aspect
@Component
@ConditionalOnClass({CircuitBreakerRegistry.class, MeterRegistry.class})
public class ResilienceMetricsAspect {

    private static final Logger log = LoggerFactory.getLogger(ResilienceMetricsAspect.class);

    private final MeterRegistry meterRegistry;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public ResilienceMetricsAspect(MeterRegistry meterRegistry,
                                    CircuitBreakerRegistry circuitBreakerRegistry) {
        this.meterRegistry = meterRegistry;
        this.circuitBreakerRegistry = circuitBreakerRegistry;
    }

    /**
     * Wrap any method annotated with {@code @CircuitBreaker} to capture
     * timing, success, and failure counters.
     */
    @Around("@annotation(circuitBreakerAnnotation)")
    public Object aroundCircuitBreaker(
            ProceedingJoinPoint pjp,
            io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker circuitBreakerAnnotation)
            throws Throwable {

        String serviceName = circuitBreakerAnnotation.name();
        String methodName  = pjp.getSignature().toShortString();
        String correlationId = MDC.get("correlationId");

        Instant start = Instant.now();
        try {
            Object result = pjp.proceed();
            long durationMs = Duration.between(start, Instant.now()).toMillis();

            successCounter(serviceName, methodName).increment();
            durationTimer(serviceName, "success").record(Duration.ofMillis(durationMs));

            log.debug("[RESILIENCE] service={} method={} outcome=SUCCESS durationMs={} correlationId={}",
                    serviceName, methodName, durationMs, correlationId);

            return result;

        } catch (Throwable ex) {
            long durationMs = Duration.between(start, Instant.now()).toMillis();
            String reason   = FallbackHandler.classifyReason(ex);

            failureCounter(serviceName, methodName, reason).increment();
            durationTimer(serviceName, "failure").record(Duration.ofMillis(durationMs));

            // Log circuit state if available
            circuitBreakerRegistry.find(serviceName).ifPresent(cb -> {
                CircuitBreaker.State state = cb.getState();
                if (state != CircuitBreaker.State.CLOSED) {
                    log.warn("[CIRCUIT] service={} state={} correlationId={}",
                            serviceName, state, correlationId);
                }
            });

            throw ex;
        }
    }

    /**
     * Wrap any method annotated with {@code @Retry} to log retry attempts.
     */
    @Around("@annotation(retryAnnotation)")
    public Object aroundRetry(
            ProceedingJoinPoint pjp,
            io.github.resilience4j.retry.annotation.Retry retryAnnotation)
            throws Throwable {

        String serviceName = retryAnnotation.name();
        try {
            return pjp.proceed();
        } catch (Throwable ex) {
            log.warn("[RETRY] service={} exception={} correlationId={}",
                    serviceName,
                    ex.getClass().getSimpleName(),
                    MDC.get("correlationId"));
            meterRegistry.counter("resilience.retry.failure",
                    "service", serviceName,
                    "reason", FallbackHandler.classifyReason(ex))
                    .increment();
            throw ex;
        }
    }

    // ──────────────────────────── meter helpers ──────────────────────────────

    private Counter successCounter(String service, String method) {
        return meterRegistry.counter("resilience.call.success",
                "service", service,
                "method",  method);
    }

    private Counter failureCounter(String service, String method, String reason) {
        return meterRegistry.counter("resilience.call.failure",
                "service", service,
                "method",  method,
                "reason",  reason);
    }

    /**
     * Called by fallback methods to increment the fallback counter.
     *
     * @param service     circuit-breaker instance name
     * @param throwable   the triggering exception
     */
    public void recordFallback(String service, Throwable throwable) {
        String reason = FallbackHandler.classifyReason(throwable);
        meterRegistry.counter("resilience.call.fallback",
                "service", service,
                "reason",  reason)
                .increment();
        log.warn("[FALLBACK-RECORDED] service={} reason={} correlationId={}",
                service, reason, MDC.get("correlationId"));
    }

    private Timer durationTimer(String service, String outcome) {
        return meterRegistry.timer("resilience.call.duration",
                "service", service,
                "outcome", outcome);
    }
}
