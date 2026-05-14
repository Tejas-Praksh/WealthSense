package com.wealthsense.investment.service;

import com.wealthsense.investment.dto.SipCalculationRequest;
import com.wealthsense.investment.dto.SipCalculationResponse;
import com.wealthsense.common.exception.WealthSenseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SipCalculatorServiceTest {

    private SipCalculatorService sipCalculatorService;

    @BeforeEach
    void setUp() {
        sipCalculatorService = new SipCalculatorService();
    }

    @Test
    void calculate_validInputs_correctFutureValue() {
        SipCalculationRequest request = SipCalculationRequest.builder()
                .monthlyAmount(new BigDecimal("5000.00")) // 5000 month
                .years(10) // 120 months
                .expectedReturn(new BigDecimal("12.0")) // 12% pa -> 1% pm
                .build();

        SipCalculationResponse response = sipCalculatorService.calculate(request);

        // 5000 * 120 = 600,000
        assertEquals(new BigDecimal("600000.00"), response.getTotalInvested());
        
        // Exact FV calculated by formula
        assertNotNull(response.getMaturityAmount());
        assertTrue(response.getMaturityAmount().compareTo(new BigDecimal("600000.00")) > 0);
        assertEquals(response.getMaturityAmount().subtract(response.getTotalInvested()), response.getEstimatedReturns());
    }

    @Test
    void calculate_zeroMonths_throwsException() {
        SipCalculationRequest request = SipCalculationRequest.builder()
                .monthlyAmount(new BigDecimal("5000"))
                .years(0)
                .expectedReturn(new BigDecimal("12.0"))
                .build();

        assertThrows(WealthSenseException.class, () -> sipCalculatorService.calculate(request));
    }

    @Test
    void calculate_zeroReturn_calculatesCorrectly() {
        SipCalculationRequest request = SipCalculationRequest.builder()
                .monthlyAmount(new BigDecimal("5000"))
                .years(5) // 60 months
                .expectedReturn(BigDecimal.ZERO)
                .build();

        SipCalculationResponse response = sipCalculatorService.calculate(request);

        assertEquals(new BigDecimal("300000.00"), response.getTotalInvested());
        assertEquals(new BigDecimal("300000.00"), response.getMaturityAmount());
        assertEquals(new BigDecimal("0.00"), response.getEstimatedReturns());
    }

    @Test
    void calculate_usesBigDecimalNotDouble() {
        // Asserting types through reflection or directly via types
        SipCalculationRequest request = SipCalculationRequest.builder()
                .monthlyAmount(new BigDecimal("1000"))
                .years(1)
                .expectedReturn(new BigDecimal("10"))
                .build();
        SipCalculationResponse response = sipCalculatorService.calculate(request);
        
        assertInstanceOf(BigDecimal.class, response.getMaturityAmount());
        assertInstanceOf(BigDecimal.class, response.getTotalInvested());
    }
}
