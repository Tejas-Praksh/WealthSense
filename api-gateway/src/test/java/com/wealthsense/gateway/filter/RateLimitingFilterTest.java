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
class RateLimitingFilterTest {

    @Mock
    private ReactiveRedisTemplate<String, String> redisTemplate;

    @Mock
    private ReactiveValueOperations<String, String> valueOps;

    private RateLimitingFilter filter;

    @BeforeEach
    void setUp() {
        filter = new RateLimitingFilter(redisTemplate, 100);
    }

    @Test
    void filter_underLimit_allowsRequest() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/test")
                .header("X-User-ID", "user-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate:user-123")).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(eq("rate:user-123"), any(Duration.class)))
                .thenReturn(Mono.just(true));
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        // Should NOT return 429
        assertNotEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());
        String remaining = exchange.getResponse().getHeaders().getFirst("X-RateLimit-Remaining");
        assertEquals("99", remaining);
    }

    @Test
    void filter_overLimit_returns429() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/v1/test")
                .header("X-User-ID", "user-123")
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate:user-123")).thenReturn(Mono.just(101L));

        filter.filter(exchange, chain).block();

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, exchange.getResponse().getStatusCode());
    }

    @Test
    void filter_differentUsers_separateLimits() {
        // User A request
        MockServerHttpRequest requestA = MockServerHttpRequest.get("/api/v1/test")
                .header("X-User-ID", "user-A")
                .build();
        MockServerWebExchange exchangeA = MockServerWebExchange.from(requestA);

        // User B request
        MockServerHttpRequest requestB = MockServerHttpRequest.get("/api/v1/test")
                .header("X-User-ID", "user-B")
                .build();
        MockServerWebExchange exchangeB = MockServerWebExchange.from(requestB);

        GatewayFilterChain chain = mock(GatewayFilterChain.class);

        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment("rate:user-A")).thenReturn(Mono.just(1L));
        when(valueOps.increment("rate:user-B")).thenReturn(Mono.just(1L));
        when(redisTemplate.expire(anyString(), any(Duration.class))).thenReturn(Mono.just(true));
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchangeA, chain).block();
        filter.filter(exchangeB, chain).block();

        // Verify separate Redis keys used
        verify(valueOps).increment("rate:user-A");
        verify(valueOps).increment("rate:user-B");
    }
}
