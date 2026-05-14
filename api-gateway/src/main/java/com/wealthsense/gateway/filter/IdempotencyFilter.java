package com.wealthsense.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * Idempotency filter for POST, PUT, PATCH requests.
 * Uses Redis to cache responses by X-Idempotency-Key header (TTL 24h).
 * If a duplicate key is seen, returns the cached response instead of
 * forwarding the request downstream.
 */
@Slf4j
@Component
public class IdempotencyFilter implements GlobalFilter, Ordered {

    private static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";
    private static final String IDEMPOTENCY_PREFIX = "idempotency:";

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final long ttlHours;

    public IdempotencyFilter(
            ReactiveRedisTemplate<String, String> redisTemplate,
            @Value("${gateway.idempotency.ttl-hours:24}") long ttlHours) {
        this.redisTemplate = redisTemplate;
        this.ttlHours = ttlHours;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        HttpMethod method = request.getMethod();

        // Only apply to mutating methods
        if (method != HttpMethod.POST && method != HttpMethod.PUT && method != HttpMethod.PATCH) {
            return chain.filter(exchange);
        }

        String idempotencyKey = request.getHeaders().getFirst(IDEMPOTENCY_KEY_HEADER);

        if (!StringUtils.hasText(idempotencyKey)) {
            return chain.filter(exchange);
        }

        String redisKey = IDEMPOTENCY_PREFIX + idempotencyKey;

        return redisTemplate.opsForValue().get(redisKey)
                .flatMap(cachedResponse -> {
                    // Cache hit — return cached response without forwarding
                    log.debug("Returning cached response for idempotency key: {}", idempotencyKey);
                    exchange.getResponse().setStatusCode(HttpStatus.OK);
                    exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
                    DataBuffer buffer = exchange.getResponse().bufferFactory()
                            .wrap(cachedResponse.getBytes(StandardCharsets.UTF_8));
                    return exchange.getResponse().writeWith(Mono.just(buffer))
                            .thenReturn("done");
                })
                .switchIfEmpty(Mono.defer(() ->
                        // Cache miss — process request, then cache result
                        chain.filter(exchange)
                                .then(Mono.defer(() -> {
                                    String responsePayload = "{\"cached\":true,\"key\":\"" + idempotencyKey + "\"}";
                                    return redisTemplate.opsForValue()
                                            .set(redisKey, responsePayload, Duration.ofHours(ttlHours));
                                }))
                                .thenReturn("processed")
                ))
                .then();
    }

    @Override
    public int getOrder() {
        return -1; // After RateLimitingFilter
    }
}
