package com.wealthsense.security.webhook;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebhookPayload {
    private String eventType;
    private String payload;
    private Instant timestamp;
    private String signature;
    private String idempotencyKey;
}

