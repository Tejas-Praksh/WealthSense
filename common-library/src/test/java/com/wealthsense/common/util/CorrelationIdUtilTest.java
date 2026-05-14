package com.wealthsense.common.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CorrelationIdUtilTest {

    @AfterEach
    void cleanup() {
        CorrelationIdUtil.clearCorrelationId();
    }

    @Test
    void generateCorrelationId_returnsNonNull() {
        String id = CorrelationIdUtil.generateCorrelationId();
        assertNotNull(id);
        assertFalse(id.isBlank());
    }

    @Test
    void generateCorrelationId_returnsUniqueValues() {
        String id1 = CorrelationIdUtil.generateCorrelationId();
        String id2 = CorrelationIdUtil.generateCorrelationId();
        assertNotEquals(id1, id2);
    }

    @Test
    void setAndGet_correlationId_workCorrectly() {
        String id = "test-correlation-id";
        CorrelationIdUtil.setCorrelationId(id);
        assertEquals(id, CorrelationIdUtil.getCurrentCorrelationId());
    }

    @Test
    void getCurrentCorrelationId_whenNotSet_generatesNew() {
        CorrelationIdUtil.clearCorrelationId();
        String id = CorrelationIdUtil.getCurrentCorrelationId();
        assertNotNull(id);
        assertFalse(id.isBlank());
    }

    @Test
    void clearCorrelationId_removesFromMdc() {
        CorrelationIdUtil.setCorrelationId("to-be-cleared");
        CorrelationIdUtil.clearCorrelationId();
        // After clear, getCurrentCorrelationId generates a new one
        String id = CorrelationIdUtil.getCurrentCorrelationId();
        assertNotEquals("to-be-cleared", id);
    }
}
