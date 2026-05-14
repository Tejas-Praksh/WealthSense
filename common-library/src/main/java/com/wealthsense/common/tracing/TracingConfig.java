package com.wealthsense.common.tracing;

import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Distributed tracing configuration using Micrometer Tracing + Brave.
 *
 * <p>Spring Boot 3.x / Spring Cloud 2023.x uses Micrometer Tracing instead
 * of Spring Cloud Sleuth. Zipkin export is configured in application.yml:
 * <pre>
 * management:
 *   tracing:
 *     sampling.probability: 1.0
 *   zipkin:
 *     tracing:
 *       endpoint: http://localhost:9411/api/v2/spans
 * </pre>
 * </p>
 *
 * <p>This config adds a servlet filter that injects {@code traceId},
 * {@code spanId}, and {@code correlationId} into SLF4J MDC so that every
 * log line carries trace context even without a structured log shipper.</p>
 */
@Configuration
@ConditionalOnClass(Tracer.class)
public class TracingConfig {

    /**
     * Filter that populates SLF4J MDC with Micrometer tracing IDs.
     * Placed before any business filter so correlation context is always present.
     */
    @Bean
    public TraceIdLoggingFilter traceIdLoggingFilter(@Autowired(required = false) Tracer tracer) {
        return new TraceIdLoggingFilter(tracer);
    }

    /**
     * Servlet filter that propagates trace context into MDC for every request.
     */
    public static class TraceIdLoggingFilter extends OncePerRequestFilter {

        /** MDC key names — match the log pattern in application.yml */
        public static final String MDC_TRACE_ID     = "traceId";
        public static final String MDC_SPAN_ID      = "spanId";
        public static final String MDC_CORRELATION  = "correlationId";

        private final Tracer tracer;

        public TraceIdLoggingFilter(Tracer tracer) {
            this.tracer = tracer;
        }

        @Override
        protected void doFilterInternal(@NonNull HttpServletRequest request,
                                        @NonNull HttpServletResponse response,
                                        @NonNull FilterChain filterChain)
                throws ServletException, IOException {

            try {
                // Populate from Micrometer Tracer if available
                if (tracer != null && tracer.currentSpan() != null) {
                    var ctx = tracer.currentSpan().context();
                    MDC.put(MDC_TRACE_ID, ctx.traceId());
                    MDC.put(MDC_SPAN_ID,  ctx.spanId());
                }

                // Correlation ID: prefer the header set by API Gateway / caller,
                // fall back to the traceId so it's always populated.
                String correlationId = request.getHeader("X-Correlation-ID");
                if (correlationId == null || correlationId.isBlank()) {
                    correlationId = MDC.get(MDC_TRACE_ID);
                }
                if (correlationId != null) {
                    MDC.put(MDC_CORRELATION, correlationId);
                    // Echo it back to the caller so the response is traceable
                    response.setHeader("X-Correlation-ID", correlationId);
                }

                filterChain.doFilter(request, response);

            } finally {
                MDC.remove(MDC_TRACE_ID);
                MDC.remove(MDC_SPAN_ID);
                MDC.remove(MDC_CORRELATION);
            }
        }
    }
}
