package com.wealthsense.gateway.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthsense.common.dto.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * Fallback controller for circuit breaker.
 * Returns 503 Service Unavailable when downstream is down.
 */
@RestController
@RequiredArgsConstructor
@Tag(name = "Gateway fallback", description = "Circuit breaker fallback responses")
public class FallbackController {

    private final ObjectMapper objectMapper;

    @Operation(summary = "Circuit breaker fallback", description = "Returned when a downstream route is unavailable")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "503", description = "Service temporarily unavailable")
    })
    @RequestMapping("/fallback")
    public Mono<String> fallback(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);

        ApiResponse<Void> response = ApiResponse.error(
                "Service temporarily unavailable. Please try again later.",
                "SERVICE_UNAVAILABLE");

        try {
            return Mono.just(objectMapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            return Mono.just("{\"success\":false,\"message\":\"Service unavailable\"}");
        }
    }
}
