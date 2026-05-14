package com.wealthsense.gateway.filter;

import com.wealthsense.security.webhook.WebhookSignatureService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

/**
 * Verifies HMAC signature for Razorpay webhook payloads.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookSignatureFilter implements GlobalFilter, Ordered {

    private static final String WEBHOOK_PATH = "/webhooks/razorpay";
    private static final String SIGNATURE_HEADER = "X-Razorpay-Signature";

    private final WebhookSignatureService signatureService;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (request.getMethod() != HttpMethod.POST || !path.startsWith(WEBHOOK_PATH)) {
            return chain.filter(exchange);
        }

        String signature = request.getHeaders().getFirst(SIGNATURE_HEADER);
        if (!StringUtils.hasText(signature)) {
            return reject(exchange, "Missing webhook signature header");
        }

        return DataBufferUtils.join(request.getBody())
                .flatMap(bodyBuffer -> {
                    byte[] bytes = new byte[bodyBuffer.readableByteCount()];
                    bodyBuffer.read(bytes);
                    DataBufferUtils.release(bodyBuffer);

                    String payload = new String(bytes, StandardCharsets.UTF_8);
                    boolean verified = signatureService.verifySignature(payload, "sha256=" + signature);
                    if (!verified) {
                        return reject(exchange, "Invalid webhook signature");
                    }

                    DataBufferFactory bufferFactory = exchange.getResponse().bufferFactory();
                    Flux<DataBuffer> cachedBody = Flux.defer(() ->
                            Flux.just(bufferFactory.wrap(bytes.clone())));

                    ServerHttpRequest decorated = new ServerHttpRequestDecorator(request) {
                        @Override
                        public Flux<DataBuffer> getBody() {
                            return cachedBody;
                        }
                    };

                    return chain.filter(exchange.mutate().request(decorated).build());
                });
    }

    private Mono<Void> reject(ServerWebExchange exchange, String message) {
        log.warn("Rejected webhook request: {}", message);
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        byte[] payload = "{\"success\":false,\"message\":\"Unauthorized webhook\"}"
                .getBytes(StandardCharsets.UTF_8);
        DataBuffer dataBuffer = exchange.getResponse().bufferFactory().wrap(payload);
        return exchange.getResponse().writeWith(Mono.just(dataBuffer));
    }

    @Override
    public int getOrder() {
        return -3;
    }
}

