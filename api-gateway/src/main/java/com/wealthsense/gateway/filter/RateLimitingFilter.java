package com.wealthsense.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.time.Duration;

/**
 * Token-bucket rate limiter using Redis. 100 requests per minute per user.
 * Authenticated users are limited by userId, unauthenticated by IP.
 */
@Slf4j
@Component
public class RateLimitingFilter implements GlobalFilter, Ordered {

    private static final String RATE_KEY_PREFIX = "rate:";
    private static final String RATE_IP_KEY_PREFIX = "rate:ip:";

    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final long requestsPerMinute;

    public RateLimitingFilter(
            ReactiveRedisTemplate<String, String> redisTemplate,
            @Value("${gateway.rate-limit.requests-per-minute:100}") long requestsPerMinute) {
        this.redisTemplate = redisTemplate;
        this.requestsPerMinute = requestsPerMinute;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String userId = request.getHeaders().getFirst("X-User-ID");

        String rateLimitKey;
        if (StringUtils.hasText(userId)) {
            rateLimitKey = RATE_KEY_PREFIX + userId;
        } else {
            InetSocketAddress remoteAddress = request.getRemoteAddress();
            String ip = remoteAddress != null ? remoteAddress.getAddress().getHostAddress() : "unknown";
            rateLimitKey = RATE_IP_KEY_PREFIX + ip;
        }

        return redisTemplate.opsForValue()
                .increment(rateLimitKey)
                .flatMap(count -> {
                    if (count == 1) {
                        return redisTemplate.expire(rateLimitKey, Duration.ofMinutes(1))
                                .thenReturn(count);
                    }
                    return Mono.just(count);
                })
                .flatMap(count -> {
                    long remaining = Math.max(0, requestsPerMinute - count);
                    exchange.getResponse().getHeaders()
                            .add("X-RateLimit-Remaining", String.valueOf(remaining));
                    exchange.getResponse().getHeaders()
                            .add("X-RateLimit-Limit", String.valueOf(requestsPerMinute));

                    if (count > requestsPerMinute) {
                        log.warn("Rate limit exceeded for key: {} (count: {})", rateLimitKey, count);
                        exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
                        return exchange.getResponse().setComplete();
                    }

                    return chain.filter(exchange);
                });
    }

    @Override
    public int getOrder() {
        return -2; // After JwtAuthenticationFilter
    }
}
