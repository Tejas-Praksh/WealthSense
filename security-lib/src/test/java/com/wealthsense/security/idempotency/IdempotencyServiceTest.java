package com.wealthsense.security.idempotency;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class IdempotencyServiceTest {

    @Test
    void check_newKey_executesAction() {
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);

        String key = "k1";
        String redisKey = "idempotent:" + key;

        when(ops.get(redisKey)).thenReturn(null);

        ObjectMapper objectMapper = new ObjectMapper();
        IdempotencyService service = new IdempotencyService(redisTemplate, objectMapper, Duration.ofHours(24));

        AtomicInteger calls = new AtomicInteger();
        IdempotencyResult<String> result = service.checkAndStore(
                key,
                "OP",
                () -> {
                    calls.incrementAndGet();
                    return "fresh-result";
                });

        assertFalse(result.isDuplicate());
        assertEquals("fresh-result", result.getResult());
        assertNotNull(result.getProcessedAt());
        assertEquals(1, calls.get());

        verify(ops).set(eq(redisKey), anyString(), eq(Duration.ofHours(24)));
    }

    @Test
    void check_duplicateKey_returnsCached() {
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);

        String key = "k2";
        String redisKey = "idempotent:" + key;

        String cachedJson = "{"
                + "\"duplicate\":true,"
                + "\"operation\":\"OP\","
                + "\"resultType\":\"java.lang.String\","
                + "\"result\":\"cached-result\","
                + "\"processedAt\":\"2020-01-01T00:00:00Z\""
                + "}";

        when(ops.get(redisKey)).thenReturn(cachedJson);

        ObjectMapper objectMapper = new ObjectMapper();
        IdempotencyService service = new IdempotencyService(redisTemplate, objectMapper, Duration.ofHours(24));

        AtomicInteger calls = new AtomicInteger();
        IdempotencyResult<String> result = service.checkAndStore(
                key,
                "OP",
                () -> {
                    calls.incrementAndGet();
                    return "should-not-run";
                });

        assertTrue(result.isDuplicate());
        assertEquals("cached-result", result.getResult());
        assertEquals(0, calls.get());
        assertEquals(Instant.parse("2020-01-01T00:00:00Z"), result.getProcessedAt());

        verify(ops, never()).set(anyString(), anyString(), any());
    }

    @Test
    void check_expired_executesAgain() {
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);

        String key = "k3";
        String redisKey = "idempotent:" + key;

        when(ops.get(redisKey)).thenReturn(null).thenReturn(null);

        ObjectMapper objectMapper = new ObjectMapper();
        IdempotencyService service = new IdempotencyService(redisTemplate, objectMapper, Duration.ofHours(24));

        AtomicInteger calls = new AtomicInteger();
        IdempotencyResult<String> first = service.checkAndStore(
                key,
                "OP",
                () -> {
                    calls.incrementAndGet();
                    return "result";
                });
        IdempotencyResult<String> second = service.checkAndStore(
                key,
                "OP",
                () -> {
                    calls.incrementAndGet();
                    return "result";
                });

        assertFalse(first.isDuplicate());
        assertFalse(second.isDuplicate());
        assertEquals(2, calls.get());

        verify(ops, times(2)).set(eq(redisKey), anyString(), eq(Duration.ofHours(24)));
    }

    @Test
    void check_actionFails_doesNotCache() {
        RedisTemplate<String, String> redisTemplate = mock(RedisTemplate.class);
        ValueOperations<String, String> ops = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(ops);

        String key = "k4";
        String redisKey = "idempotent:" + key;

        when(ops.get(redisKey)).thenReturn(null);

        ObjectMapper objectMapper = new ObjectMapper();
        IdempotencyService service = new IdempotencyService(redisTemplate, objectMapper, Duration.ofHours(24));

        assertThrows(RuntimeException.class, () -> service.checkAndStore(
                key,
                "OP",
                () -> {
                    throw new RuntimeException("boom");
                }));

        verify(ops, never()).set(anyString(), anyString(), any());
    }
}

