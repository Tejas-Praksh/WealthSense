package com.wealthsense.security.idempotency;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.time.Duration;
import java.time.Instant;

@Service
public class IdempotencyService {

    private static final String IDEMPOTENCY_PREFIX = "idempotent:";

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;
    private final Duration ttl;

    public IdempotencyService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            @Value("${security-lib.idempotency.ttl-hours:24}") long ttlHours) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.objectMapper.findAndRegisterModules();
        Assert.isTrue(ttlHours > 0, "ttlHours must be > 0");
        this.ttl = Duration.ofHours(ttlHours);
    }

    // Visible for tests.
    public IdempotencyService(
            RedisTemplate<String, String> redisTemplate,
            ObjectMapper objectMapper,
            Duration ttl) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.objectMapper.findAndRegisterModules();
        this.ttl = ttl;
    }

    public <T> IdempotencyResult<T> checkAndStore(
            String key,
            String operation,
            java.util.function.Supplier<T> action) {

        Assert.hasText(key, "Idempotency key must be provided.");
        Assert.notNull(action, "Action supplier must not be null.");

        String redisKey = IDEMPOTENCY_PREFIX + key;

        String cachedJson = redisTemplate.opsForValue().get(redisKey);
        Instant now = Instant.now();

        if (cachedJson != null && !cachedJson.isBlank()) {
            try {
                JsonNode root = objectMapper.readTree(cachedJson);
                // If this key exists in Redis, we treat it as already processed.

                String resultType = root.path("resultType").asText();
                JsonNode resultNode = root.path("result");
                Instant processedAt = Instant.parse(root.path("processedAt").asText());

                @SuppressWarnings("unchecked")
                T cachedResult;
                if ("null".equals(resultType) || resultNode.isNull()) {
                    cachedResult = null;
                } else {
                    Class<?> clazz = Class.forName(resultType);
                    cachedResult = (T) objectMapper.treeToValue(resultNode, clazz);
                }
                return IdempotencyResult.duplicate(cachedResult, processedAt);
            } catch (Exception ignored) {
                // If parsing fails, fall through to fresh execution.
            }
        }

        return freshFromAction(key, operation, action, now);
    }

    private <T> IdempotencyResult<T> freshFromAction(
            String key,
            String operation,
            java.util.function.Supplier<T> action,
            Instant now) {

        String redisKey = IDEMPOTENCY_PREFIX + key;

        T result = action.get(); // Supplier exceptions propagate; we must not cache failures.

        StoredIdempotencyValue<T> stored = new StoredIdempotencyValue<>(
                true,
                operation,
                result == null ? "null" : result.getClass().getName(),
                result,
                now);

        try {
            String valueJson = objectMapper.writeValueAsString(stored);
            redisTemplate.opsForValue().set(redisKey, valueJson, ttl);
        } catch (Exception e) {
            // Do not fail the business operation if caching fails.
        }

        return IdempotencyResult.fresh(result, now);
    }

    private static final class StoredIdempotencyValue<T> {
        private final boolean duplicate;
        private final String operation;
        private final String resultType;
        private final T result;
        private final Instant processedAt;

        private StoredIdempotencyValue(
                boolean duplicate,
                String operation,
                String resultType,
                T result,
                Instant processedAt) {
            this.duplicate = duplicate;
            this.operation = operation;
            this.resultType = resultType;
            this.result = result;
            this.processedAt = processedAt;
        }

        @SuppressWarnings("unused")
        public boolean isDuplicate() {
            return duplicate;
        }

        @SuppressWarnings("unused")
        public String getOperation() {
            return operation;
        }

        @SuppressWarnings("unused")
        public String getResultType() {
            return resultType;
        }

        @SuppressWarnings("unused")
        public T getResult() {
            return result;
        }

        @SuppressWarnings("unused")
        public Instant getProcessedAt() {
            return processedAt;
        }
    }
}

