package com.wealthsense.common.resilience;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeoutException;

/**
 * Centralised fallback utilities for Resilience4J fallback methods.
 *
 * <p>Every fallback method should call {@link #logFallback} so that:
 * <ul>
 *   <li>circuit-open events are distinguishable from timeout events in logs</li>
 *   <li>correlation IDs remain in every log line</li>
 *   <li>Prometheus can scrape the reason counter via
 *       {@link ResilienceMetricsAspect}</li>
 * </ul>
 * </p>
 *
 * <p>Secrets must NEVER be included in the log message — only the service
 * name, correlation ID, and exception type.</p>
 */
public final class FallbackHandler {

    private static final Logger log = LoggerFactory.getLogger(FallbackHandler.class);

    private FallbackHandler() {
        // utility class
    }

    /**
     * Classify and log the fallback reason.
     *
     * @param serviceName     logical name of the circuit-breaker instance
     * @param correlationId   request correlation ID (from MDC / event header)
     * @param throwable       exception that triggered the fallback
     */
    public static void logFallback(String serviceName,
                                   String correlationId,
                                   Throwable throwable) {
        String reason = classifyReason(throwable);
        log.warn("[FALLBACK] service={} correlationId={} reason={} exception={}",
                serviceName,
                correlationId != null ? correlationId : "unknown",
                reason,
                throwable.getClass().getSimpleName());
    }

    /**
     * Classify the exception into a human-readable reason tag used in
     * Prometheus counters.
     *
     * @param throwable the exception
     * @return short reason string
     */
    public static String classifyReason(Throwable throwable) {
        if (throwable instanceof CallNotPermittedException) {
            return "CIRCUIT_OPEN";
        }
        if (throwable instanceof BulkheadFullException) {
            return "BULKHEAD_FULL";
        }
        if (throwable instanceof TimeoutException) {
            return "TIMEOUT";
        }
        if (throwable instanceof RequestNotPermitted) {
            return "RATE_LIMITED";
        }
        if (throwable instanceof java.net.ConnectException
                || throwable instanceof java.net.SocketTimeoutException) {
            return "NETWORK_ERROR";
        }
        return "UNKNOWN_" + throwable.getClass().getSimpleName().toUpperCase();
    }

    /**
     * Build a standard manual-review fallback message when a downstream
     * service is unavailable.
     *
     * @param reason human-readable reason
     * @return formatted message safe to surface to callers
     */
    public static String manualReviewMessage(String reason) {
        return "Service unavailable — flagged for manual review. Reason: " + reason;
    }
}
