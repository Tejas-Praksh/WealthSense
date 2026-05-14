package com.wealthsense.common.util;

import org.slf4j.MDC;

import java.util.UUID;

public final class CorrelationIdUtil {

    private static final String MDC_KEY = "correlationId";

    private CorrelationIdUtil() {
        throw new UnsupportedOperationException("Utility class — do not instantiate");
    }

    public static String generateCorrelationId() {
        return UUID.randomUUID().toString();
    }

    public static void setCorrelationId(String id) {
        MDC.put(MDC_KEY, id);
    }

    public static String getCurrentCorrelationId() {
        String id = MDC.get(MDC_KEY);
        if (id == null || id.isBlank()) {
            id = generateCorrelationId();
            setCorrelationId(id);
        }
        return id;
    }

    public static void clearCorrelationId() {
        MDC.remove(MDC_KEY);
    }
}
