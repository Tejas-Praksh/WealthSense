package com.wealthsense.gateway.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Programmatic route definitions for all WealthSense microservices.
 * Each route includes a Resilience4j circuit breaker with fallback.
 */
@Configuration
public class RouteConfig {

    @Bean
    public RouteLocator customRouteLocator(
            RouteLocatorBuilder builder,
            @Value("${USER_SERVICE_URL:http://localhost:8081}") String userServiceUrl,
            @Value("${TRANSACTION_SERVICE_URL:http://localhost:8082}") String transactionServiceUrl,
            @Value("${FRAUD_SERVICE_URL:http://localhost:8083}") String fraudServiceUrl,
            @Value("${DECISION_ENGINE_SERVICE_URL:http://localhost:8084}") String decisionEngineServiceUrl,
            @Value("${NOTIFICATION_SERVICE_URL:http://localhost:8085}") String notificationServiceUrl,
            @Value("${AI_ADVISOR_SERVICE_URL:http://localhost:8086}") String aiAdvisorServiceUrl,
            @Value("${INVESTMENT_SERVICE_URL:http://localhost:8087}") String investmentServiceUrl) {
        return builder.routes()
                .route("user-service-auth", r -> r
                        .path("/api/v1/auth/**")
                        .filters(f -> f.circuitBreaker(cb -> cb
                                .setName("user-service")
                                .setFallbackUri("forward:/fallback")))
                        .uri(userServiceUrl))

                .route("user-service-users", r -> r
                        .path("/api/v1/users/**")
                        .filters(f -> f.circuitBreaker(cb -> cb
                                .setName("user-service")
                                .setFallbackUri("forward:/fallback")))
                        .uri(userServiceUrl))

                .route("transaction-service", r -> r
                        .path("/api/v1/transactions/**")
                        .filters(f -> f.circuitBreaker(cb -> cb
                                .setName("transaction-service")
                                .setFallbackUri("forward:/fallback")))
                        .uri(transactionServiceUrl))

                .route("fraud-detection-service", r -> r
                        .path("/api/v1/fraud/**")
                        .filters(f -> f.circuitBreaker(cb -> cb
                                .setName("fraud-detection-service")
                                .setFallbackUri("forward:/fallback")))
                        .uri(fraudServiceUrl))

                .route("decision-engine-service", r -> r
                        .path("/api/v1/decisions/**")
                        .filters(f -> f.circuitBreaker(cb -> cb
                                .setName("decision-engine-service")
                                .setFallbackUri("forward:/fallback")))
                        .uri(decisionEngineServiceUrl))

                .route("notification-service", r -> r
                        .path("/api/v1/notifications/**")
                        .filters(f -> f.circuitBreaker(cb -> cb
                                .setName("notification-service")
                                .setFallbackUri("forward:/fallback")))
                        .uri(notificationServiceUrl))

                .route("ai-advisor-service", r -> r
                        .path("/api/v1/ai/**")
                        .filters(f -> f.circuitBreaker(cb -> cb
                                .setName("ai-advisor-service")
                                .setFallbackUri("forward:/fallback")))
                        .uri(aiAdvisorServiceUrl))

                .route("investment-service", r -> r
                        .path("/api/v1/investments/**")
                        .filters(f -> f.circuitBreaker(cb -> cb
                                .setName("investment-service")
                                .setFallbackUri("forward:/fallback")))
                        .uri(investmentServiceUrl))

                .build();
    }
}
