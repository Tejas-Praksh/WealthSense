package com.wealthsense.security.masking;

import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class DataMaskingServiceTest {

    @Test
    void maskEmail_validEmail_masksCorrectly() {
        DataMaskingService service = new DataMaskingService();
        assertEquals("jo***@gmail.com", service.maskEmail("john.doe@gmail.com"));
    }

    @Test
    void maskPhone_validPhone_masksCorrectly() {
        DataMaskingService service = new DataMaskingService();
        assertEquals("******3210", service.maskPhone("9876543210"));
    }

    @Test
    void maskAccountNumber_masksCorrectly() {
        DataMaskingService service = new DataMaskingService();
        assertEquals("******7890", service.maskAccountNumber("1234567890"));
    }
}

