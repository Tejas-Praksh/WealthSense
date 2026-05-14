package com.wealthsense.notification.filter;

import com.wealthsense.common.constants.SecurityConstants;
import com.wealthsense.common.util.CorrelationIdUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class CorrelationIdFilterTest {

    private final CorrelationIdFilter filter = new CorrelationIdFilter();

    @AfterEach
    void tearDown() {
        CorrelationIdUtil.clearCorrelationId();
    }

    @Test
    void doFilterInternal_whenHeaderMissing_generatesAndReturnsCorrelationId() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        String correlationId = response.getHeader(SecurityConstants.CORRELATION_ID_HEADER);
        assertNotNull(correlationId);
        assertFalse(correlationId.isBlank());
        assertNull(MDC.get("correlationId"));
    }

    @Test
    void doFilterInternal_whenHeaderPresent_preservesCorrelationId() throws Exception {
        String existingCorrelationId = "corr-456";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(SecurityConstants.CORRELATION_ID_HEADER, existingCorrelationId);
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        assertEquals(existingCorrelationId, response.getHeader(SecurityConstants.CORRELATION_ID_HEADER));
        assertNull(MDC.get("correlationId"));
    }
}
