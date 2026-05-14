package com.wealthsense.common.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wealthsense.common.util.CorrelationIdUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ApiResponseTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        CorrelationIdUtil.setCorrelationId("test-corr-id");
    }

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clearCorrelationId();
    }

    @Test
    void success_factory_setsSuccessTrue() {
        ApiResponse<String> response =
                ApiResponse.success("data", "Operation successful");

        assertTrue(response.isSuccess());
        assertEquals("data", response.getData());
        assertEquals("Operation successful", response.getMessage());
        assertNotNull(response.getTimestamp());
        assertNotNull(response.getCorrelationId());
        assertNull(response.getErrorCode());
    }

    @Test
    void error_factory_setsSuccessFalse() {
        ApiResponse<Void> response =
                ApiResponse.error("Not found", "RESOURCE_NOT_FOUND");

        assertFalse(response.isSuccess());
        assertEquals("RESOURCE_NOT_FOUND", response.getErrorCode());
        assertEquals("Not found", response.getMessage());
        assertNull(response.getData());
        assertNotNull(response.getTimestamp());
    }

    @Test
    void jsonSerialization_hidesNullFields() throws Exception {
        ApiResponse<String> response =
                ApiResponse.success("test", "OK");

        String json = objectMapper.writeValueAsString(response);

        assertFalse(json.contains("errorCode"),
                "Null errorCode should be excluded from JSON");
        assertTrue(json.contains("\"success\":true"));
        assertTrue(json.contains("\"data\":\"test\""));
    }

    @Test
    void errorResponse_includesErrorCodeInJson() throws Exception {
        ApiResponse<Void> response =
                ApiResponse.error("Fail", "VALIDATION_FAILED");

        String json = objectMapper.writeValueAsString(response);

        assertTrue(json.contains("VALIDATION_FAILED"));
        assertFalse(json.contains("\"data\""),
                "Null data should be excluded from JSON");
    }
}
