package com.wealthsense.common.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.bulkhead.BulkheadRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.EnableAspectJAutoProxy;

/**
 * Global Resilience4J configuration.
 *
 * <p>All circuit-breaker instances are driven by application.yml. This class
 * simply ensures AOP proxying is active and documents the expected registries
 * so services can inject them if needed.</p>
 *
 * <p>Instance configuration (sliding window, threshold, wait duration, etc.)
 * lives in each service's {@code application.yml} under
 * {@code resilience4j.circuitbreaker.instances.*}.</p>
 */
@Configuration
@EnableAspectJAutoProxy(proxyTargetClass = true)
public class ResilienceConfig {

    /**
     * Exposes the auto-configured {@link CircuitBreakerRegistry} bean for
     * services that want to inspect circuit-breaker state programmatically
     * (e.g. health indicators, metrics).
     */
    @Bean
    public static CircuitBreakerRegistry circuitBreakerRegistry(
            CircuitBreakerRegistry registry) {
        return registry;
    }

    /**
     * Exposes the auto-configured {@link RetryRegistry} bean.
     */
    @Bean
    public static RetryRegistry retryRegistry(RetryRegistry registry) {
        return registry;
    }

    /**
     * Exposes the auto-configured {@link BulkheadRegistry} bean.
     */
    @Bean
    public static BulkheadRegistry bulkheadRegistry(BulkheadRegistry registry) {
        return registry;
    }

    /**
     * Exposes the auto-configured {@link TimeLimiterRegistry} bean.
     */
    @Bean
    public static TimeLimiterRegistry timeLimiterRegistry(TimeLimiterRegistry registry) {
        return registry;
    }
}
