package com.wealthsense.common.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

/**
 * Actuator health indicator for the Fraud Detection Service.
 *
 * <p>Checks two things:
 * <ol>
 *   <li>HTTP reachability via {@code GET /actuator/health/liveness}</li>
 *   <li>Current Resilience4J circuit-breaker state for "fraudService"</li>
 * </ol>
 * </p>
 *
 * <p>Reports {@code DOWN} if the circuit is OPEN (even if the HTTP probe
 * succeeds), because an open circuit means traffic is already being shed.</p>
 */
@Component
@ConditionalOnProperty(
        name  = "wealthsense.health.fraud-service.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class FraudServiceHealthIndicator extends AbstractHealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(FraudServiceHealthIndicator.class);

    @Value("${wealthsense.health.fraud-service.url:http://localhost:8083}")
    private String fraudServiceUrl;

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RestClient restClient;

    public FraudServiceHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry) {
        super("Fraud service health check failed");
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.restClient = RestClient.builder()
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        CircuitBreaker.State cbState = circuitBreakerRegistry
                .find("fraudService")
                .map(CircuitBreaker::getState)
                .orElse(CircuitBreaker.State.CLOSED);

        builder.withDetail("circuitBreakerState", cbState.name());

        // If circuit is OPEN we already know the service is degraded
        if (cbState == CircuitBreaker.State.OPEN) {
            log.warn("[HEALTH] FraudService circuit OPEN — reporting DOWN");
            builder.down().withDetail("reason", "Circuit breaker OPEN");
            return;
        }

        try {
            restClient.get()
                    .uri(fraudServiceUrl + "/actuator/health/liveness")
                    .retrieve()
                    .toBodilessEntity();

            log.debug("[HEALTH] FraudService liveness probe OK, circuitState={}", cbState);
            builder.up()
                   .withDetail("url", fraudServiceUrl)
                   .withDetail("circuitBreakerState", cbState.name());

        } catch (Exception ex) {
            log.warn("[HEALTH] FraudService unreachable: {}", ex.getMessage());
            builder.down()
                   .withDetail("url", fraudServiceUrl)
                   .withDetail("error", ex.getMessage());
        }
    }
}
