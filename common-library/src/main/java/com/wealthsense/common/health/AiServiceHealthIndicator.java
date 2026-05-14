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

/**
 * Actuator health indicator for the AI Advisor Service (Claude API backend).
 *
 * <p>Checks:
 * <ol>
 *   <li>HTTP reachability via {@code GET /actuator/health/liveness}</li>
 *   <li>Current Resilience4J circuit-breaker state for "aiAdvisorService"</li>
 * </ol>
 * </p>
 *
 * <p>Rate-limit state is not exposed in health details because that would
 * leak API quota information to unauthenticated callers of
 * {@code /actuator/health}.</p>
 */
@Component
@ConditionalOnProperty(
        name  = "wealthsense.health.ai-service.enabled",
        havingValue = "true",
        matchIfMissing = false
)
public class AiServiceHealthIndicator extends AbstractHealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(AiServiceHealthIndicator.class);

    @Value("${wealthsense.health.ai-service.url:http://localhost:8086}")
    private String aiServiceUrl;

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RestClient restClient;

    public AiServiceHealthIndicator(CircuitBreakerRegistry circuitBreakerRegistry) {
        super("AI Advisor service health check failed");
        this.circuitBreakerRegistry = circuitBreakerRegistry;
        this.restClient = RestClient.builder()
                .defaultHeader("Accept", "application/json")
                .build();
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) {
        CircuitBreaker.State cbState = circuitBreakerRegistry
                .find("aiAdvisorService")
                .map(CircuitBreaker::getState)
                .orElse(CircuitBreaker.State.CLOSED);

        builder.withDetail("circuitBreakerState", cbState.name());

        if (cbState == CircuitBreaker.State.OPEN) {
            log.warn("[HEALTH] AiAdvisorService circuit OPEN — reporting DOWN");
            builder.down().withDetail("reason", "Circuit breaker OPEN");
            return;
        }

        try {
            restClient.get()
                    .uri(aiServiceUrl + "/actuator/health/liveness")
                    .retrieve()
                    .toBodilessEntity();

            log.debug("[HEALTH] AiAdvisorService liveness probe OK, circuitState={}", cbState);
            builder.up()
                   .withDetail("url", aiServiceUrl)
                   .withDetail("circuitBreakerState", cbState.name());

        } catch (Exception ex) {
            log.warn("[HEALTH] AiAdvisorService unreachable: {}", ex.getMessage());
            builder.down()
                   .withDetail("error", ex.getMessage());
        }
    }
}
