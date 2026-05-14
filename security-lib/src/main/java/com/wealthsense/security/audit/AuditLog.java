package com.wealthsense.security.audit;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;

@Document(collection = "audit_logs")
public class AuditLog {

    @Id
    private String id;

    private UUID userId;
    private String action;
    private String resource;
    private String resourceId;
    private String ipAddress;
    private String userAgent;
    private String correlationId;

    private boolean success;
    private String errorMessage;

    private Instant timestamp;
    private Long durationMs;

    public AuditLog() {
    }

    public AuditLog(String id,
                     UUID userId,
                     String action,
                     String resource,
                     String resourceId,
                     String ipAddress,
                     String userAgent,
                     String correlationId,
                     boolean success,
                     String errorMessage,
                     Instant timestamp,
                     Long durationMs) {
        this.id = id;
        this.userId = userId;
        this.action = action;
        this.resource = resource;
        this.resourceId = resourceId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
        this.correlationId = correlationId;
        this.success = success;
        this.errorMessage = errorMessage;
        this.timestamp = timestamp;
        this.durationMs = durationMs;
    }

    public String getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getAction() {
        return action;
    }

    public String getResource() {
        return resource;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getUserAgent() {
        return userAgent;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public Long getDurationMs() {
        return durationMs;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public void setCorrelationId(String correlationId) {
        this.correlationId = correlationId;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }

    public void setDurationMs(Long durationMs) {
        this.durationMs = durationMs;
    }
}

