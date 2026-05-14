package com.wealthsense.common.health;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.apache.kafka.clients.admin.AdminClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TDD tests for custom health indicators.
 *
 * <p>Uses Mockito only — no Spring context, no real Kafka/Redis/DB needed.</p>
 */
@ExtendWith(MockitoExtension.class)
class HealthIndicatorTest {

    // ──────────────────────────── KafkaHealthIndicator ───────────────────────

    @Test
    @DisplayName("Kafka health indicator returns DOWN when bootstrap servers not configured")
    void kafka_notConfigured_returnsUnknown() throws Exception {
        KafkaHealthIndicator indicator = new KafkaHealthIndicator();
        ReflectionTestUtils.setField(indicator, "bootstrapServers", "");

        Health.Builder builder = new Health.Builder();
        indicator.doHealthCheck(builder);
        Health health = builder.build();

        assertThat(health.getStatus()).isEqualTo(Status.UNKNOWN);
        assertThat(health.getDetails()).containsKey("kafka");
    }

    @Test
    @DisplayName("Kafka health indicator returns DOWN when broker is unreachable")
    void kafka_disconnected_returnsDown() throws Exception {
        KafkaHealthIndicator indicator = new KafkaHealthIndicator();
        // Point to a port that refuses connections
        ReflectionTestUtils.setField(indicator, "bootstrapServers", "localhost:19999");

        Health.Builder builder = new Health.Builder();
        indicator.doHealthCheck(builder);
        Health health = builder.build();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    // ──────────────────────────── FraudServiceHealthIndicator ─────────────────

    @Test
    @DisplayName("FraudService indicator reports DOWN when circuit breaker is OPEN")
    void fraudService_circuitOpen_reportsDown() throws Exception {
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(2)
                .failureRateThreshold(50)
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker cb = registry.circuitBreaker("fraudService");

        // Force OPEN
        cb.transitionToOpenState();

        FraudServiceHealthIndicator indicator = new FraudServiceHealthIndicator(registry);
        ReflectionTestUtils.setField(indicator, "fraudServiceUrl", "http://localhost:9999");

        Health.Builder builder = new Health.Builder();
        indicator.doHealthCheck(builder);
        Health health = builder.build();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("circuitBreakerState")).isEqualTo("OPEN");
    }

    @Test
    @DisplayName("FraudService indicator reports DOWN when HTTP call fails (circuit CLOSED)")
    void fraudService_unreachable_circuitClosed_reportsDown() throws Exception {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();
        // Circuit is CLOSED by default

        FraudServiceHealthIndicator indicator = new FraudServiceHealthIndicator(registry);
        // Use a port that refuses connections
        ReflectionTestUtils.setField(indicator, "fraudServiceUrl", "http://localhost:19999");

        Health.Builder builder = new Health.Builder();
        indicator.doHealthCheck(builder);
        Health health = builder.build();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }

    // ──────────────────────────── AiServiceHealthIndicator ────────────────────

    @Test
    @DisplayName("AiService indicator reports DOWN when circuit breaker is OPEN")
    void aiService_circuitOpen_reportsDown() throws Exception {
        CircuitBreakerConfig config = CircuitBreakerConfig.ofDefaults();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        CircuitBreaker cb = registry.circuitBreaker("aiAdvisorService");

        cb.transitionToOpenState();

        AiServiceHealthIndicator indicator = new AiServiceHealthIndicator(registry);
        ReflectionTestUtils.setField(indicator, "aiServiceUrl", "http://localhost:9999");

        Health.Builder builder = new Health.Builder();
        indicator.doHealthCheck(builder);
        Health health = builder.build();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        assertThat(health.getDetails().get("circuitBreakerState")).isEqualTo("OPEN");
    }

    @Test
    @DisplayName("AiService indicator reports DOWN when HTTP call fails (circuit CLOSED)")
    void aiService_unreachable_circuitClosed_reportsDown() throws Exception {
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.ofDefaults();

        AiServiceHealthIndicator indicator = new AiServiceHealthIndicator(registry);
        ReflectionTestUtils.setField(indicator, "aiServiceUrl", "http://localhost:19999");

        Health.Builder builder = new Health.Builder();
        indicator.doHealthCheck(builder);
        Health health = builder.build();

        assertThat(health.getStatus()).isEqualTo(Status.DOWN);
    }
}
