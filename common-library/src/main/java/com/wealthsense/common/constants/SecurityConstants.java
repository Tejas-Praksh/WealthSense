package com.wealthsense.common.constants;

public final class SecurityConstants {

    private SecurityConstants() {
        throw new UnsupportedOperationException("Constants class — do not instantiate");
    }

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    public static final String IDEMPOTENCY_KEY_HEADER = "X-Idempotency-Key";
    public static final String RATE_LIMIT_HEADER = "X-RateLimit-Remaining";

    public static final long JWT_EXPIRATION_MS = 900_000L;        // 15 minutes
    public static final long REFRESH_EXPIRATION_MS = 604_800_000L; // 7 days

    public static final int MAX_LOGIN_ATTEMPTS = 5;
    public static final int RATE_LIMIT_PER_MINUTE = 100;
}
