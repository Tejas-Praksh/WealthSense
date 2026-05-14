package com.wealthsense.common.health;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Custom Actuator health indicator for Apache Kafka.
 *
 * <p>Reports:
 * <ul>
 *   <li>{@code UP} — when {@code AdminClient.listTopics()} succeeds within 3 s</li>
 *   <li>{@code DOWN} — when the call fails or times out</li>
 *   <li>{@code UNKNOWN} — when bootstrap servers are not configured</li>
 * </ul>
 * </p>
 *
 * <p>Health details never include credentials or connection strings —
 * only the broker list hostname(s) and the result status.</p>
 */
@Component
public class KafkaHealthIndicator extends AbstractHealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(KafkaHealthIndicator.class);
    private static final int TIMEOUT_SECONDS = 3;

    @Value("${spring.kafka.bootstrap-servers:localhost:9092}")
    private String bootstrapServers;

    public KafkaHealthIndicator() {
        super("Kafka health check failed");
    }

    @Override
    protected void doHealthCheck(Health.Builder builder) throws Exception {
        if (bootstrapServers == null || bootstrapServers.isBlank()) {
            builder.unknown()
                   .withDetail("kafka", "bootstrap-servers not configured");
            return;
        }

        Map<String, Object> config = Map.of(
                AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers,
                AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, String.valueOf(TIMEOUT_SECONDS * 1000),
                AdminClientConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, String.valueOf(TIMEOUT_SECONDS * 1000)
        );

        try (AdminClient adminClient = AdminClient.create(config)) {
            var names = adminClient.listTopics()
                                   .names()
                                   .get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
            builder.up()
                   .withDetail("topicCount", names.size())
                   .withDetail("brokers", sanitiseBrokers(bootstrapServers));
            log.debug("[HEALTH] Kafka UP, topicCount={}", names.size());

        } catch (Exception ex) {
            log.warn("[HEALTH] Kafka DOWN: {}", ex.getMessage());
            builder.down()
                   .withDetail("brokers", sanitiseBrokers(bootstrapServers))
                   .withException(ex);
        }
    }

    /** Return only host:port pairs — never usernames/passwords. */
    private String sanitiseBrokers(String raw) {
        // Strip any embedded credentials (e.g. sasl+ssl schemes) for safety
        return raw.replaceAll("://[^@]+@", "://***@");
    }
}
