package com.wealthsense.gateway.filter;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @Test
    void filter_noCorrelationId_generatesNew() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/test").build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        String correlationId = responseHeaders.getFirst("X-Correlation-ID");
        assertNotNull(correlationId);
        assertFalse(correlationId.isBlank());
    }

    @Test
    void filter_existingCorrelationId_preservesIt() {
        String existingId = "existing-corr-id-123";
        MockServerHttpRequest request = MockServerHttpRequest.get("/test")
                .header("X-Correlation-ID", existingId)
                .build();
        MockServerWebExchange exchange = MockServerWebExchange.from(request);
        GatewayFilterChain chain = mock(GatewayFilterChain.class);
        when(chain.filter(any())).thenReturn(Mono.empty());

        filter.filter(exchange, chain).block();

        HttpHeaders responseHeaders = exchange.getResponse().getHeaders();
        String correlationId = responseHeaders.getFirst("X-Correlation-ID");
        assertEquals(existingId, correlationId);
    }
}
