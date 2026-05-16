package com.wealthsense.security.aop;

import com.wealthsense.common.util.CorrelationIdUtil;
import com.wealthsense.security.audit.AuditLog;
import com.wealthsense.security.audit.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Aspect
@Component
@ConditionalOnBean(AuditService.class)
public class SecurityAuditAspect {

    private final AuditService auditService;

    public SecurityAuditAspect(AuditService auditService) {
        this.auditService = auditService;
    }

    @Around("@annotation(auditable)")
    public Object audit(ProceedingJoinPoint joinPoint, Auditable auditable) throws Throwable {
        long startNanos = System.nanoTime();
        boolean success = false;
        String errorMessage = null;

        String correlationId = safeCorrelationId();
        UUID userId = extractUserId();

        HttpServletRequest request = currentRequest();
        String ip = request != null ? request.getRemoteAddr() : null;
        String userAgent = request != null ? request.getHeader("User-Agent") : null;

        String resourceId = extractResourceId(joinPoint);

        try {
            Object result = joinPoint.proceed();
            success = true;
            return result;
        } catch (Throwable ex) {
            success = false;
            errorMessage = sanitizeErrorMessage(ex.getMessage());
            throw ex;
        } finally {
            long durationMs = Math.max(0, (System.nanoTime() - startNanos) / 1_000_000);
            AuditLog auditLog = new AuditLog();
            auditLog.setId(UUID.randomUUID().toString());
            auditLog.setUserId(userId);
            auditLog.setAction(auditable.action());
            auditLog.setResource(auditable.resource());
            auditLog.setResourceId(resourceId);
            auditLog.setIpAddress(ip);
            auditLog.setUserAgent(userAgent);
            auditLog.setCorrelationId(correlationId);
            auditLog.setSuccess(success);
            auditLog.setErrorMessage(errorMessage);
            auditLog.setTimestamp(Instant.now());
            auditLog.setDurationMs(durationMs);

            // Never persist sensitive request/response payloads — metadata only.
            auditService.save(auditLog);
        }
    }

    private String safeCorrelationId() {
        try {
            return CorrelationIdUtil.getCurrentCorrelationId();
        } catch (Exception e) {
            // Fallback to MDC if available.
            String mdcId = MDC.get("correlationId");
            return mdcId != null ? mdcId : null;
        }
    }

    private UUID extractUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }

        Object principal = authentication.getPrincipal();
        if (principal instanceof UUID uuid) {
            return uuid;
        }
        if (principal instanceof String s) {
            try {
                return UUID.fromString(s);
            } catch (Exception ignored) {
                return null;
            }
        }
        return null;
    }

    private String extractResourceId(ProceedingJoinPoint joinPoint) {
        // Best-effort: use first UUID argument.
        Object[] args = joinPoint.getArgs();
        if (args == null) {
            return null;
        }
        Optional<UUID> firstUuid = Arrays.stream(args)
                .filter(a -> a instanceof UUID)
                .map(a -> (UUID) a)
                .findFirst();
        return firstUuid.map(UUID::toString).orElse(null);
    }

    private HttpServletRequest currentRequest() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            return null;
        }
        return attrs.getRequest();
    }

    private String sanitizeErrorMessage(String message) {
        if (message == null) {
            return null;
        }
        // Basic hardening: redact common password/key patterns.
        return message
                .replaceAll("(?i)password\\s*[:=]\\s*\\S+", "[REDACTED]")
                .replaceAll("(?i)password", "[REDACTED]");
    }

}

