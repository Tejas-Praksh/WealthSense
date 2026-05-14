package com.wealthsense.gateway.filter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveValueOperations;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IdempotencyFilterTest {

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    private IdempotencyFilter filter;

    @BeforeEach
    void setUp() {
        filter = new IdempotencyFilter(redisTemplate, 24);
    }

    @Test
    void filter_newKey_processesRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/transactions")
                .header("X-Idempotency-Key", "unique-key-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("idempotency:unique-key-123")).thenReturn(Mono.empty());
        when(chain.filter(any())).thenReturn(Mono.empty());
        when(valueOps.set(eq("idempotency:unique-key-123"), anyString(), any(Duration.class)))
                .thenReturn(Mono.just(true));

        filter.filter(exchange, chain).block();

        // Chain was called — request was processed
        verify(chain).filter(any());
    }

    @Test
    void filter_duplicateKey_returnsCachedResponse() {
        String cachedJson = "{\"success\":true,\"message\":\"Already processed\"}";

        MockServerHttpRequest request = MockServerHttpRequest.post("/api/v1/transactions")
                .header("X-Idempotency-Key", "duplicate-key")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.get("idempotency:duplicate-key")).thenReturn(Mono.just(cachedJson));

        filter.filter(exchange, chain).block();

        // Chain should NOT have been called — returned cached response
        verify(chain, never()).filter(any());
        assertEquals(HttpStatus.OK, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_getRequest_skipsIdempotencyCheck() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/transactions")
                .header("X-Idempotency-Key", "some-key")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        // GET skips idempotency entirely — chain called, Redis NOT consulted
        verify(chain).filter(any());
        verifyNoInteractions(redisTemplate);
    }
}
