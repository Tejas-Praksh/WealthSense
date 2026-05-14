package com.wealthsense.investment.service;

import com.wealthsense.investment.domain.Investment;
import com.wealthsense.investment.domain.InvestmentStatus;
import com.wealthsense.investment.domain.InvestmentType;
import com.wealthsense.investment.dto.PortfolioSummaryDto;
import com.wealthsense.investment.repository.InvestmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InvestmentServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private InvestmentService investmentService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void getRecommendations_lowIncome_returnsConservative() {
        List<String> result = investmentService.getRecommendations(userId, new BigDecimal("30000"));
        assertTrue(result.contains("Conservative (FD, PPF)"));
    }

    @Test
    void getRecommendations_highIncome_returnsAggressive() {
        List<String> result = investmentService.getRecommendations(userId, new BigDecimal("200000"));
        assertTrue(result.contains("Aggressive (ELSS, Index)"));
    }

    @Test
    void getPortfolio_withInvestments_returnsCorrectSummary() {
        Investment i1 = Investment.builder()
                .type(InvestmentType.SIP)
                .amountPaise(new BigDecimal("10000000")) // 1L invested
                .currentValuePaise(new BigDecimal("12000000")) // 1.2L current
                .build();

        Investment i2 = Investment.builder()
                .type(InvestmentType.LUMPSUM)
                .amountPaise(new BigDecimal("5000000")) // 50k invested
                .currentValuePaise(new BigDecimal("6000000")) // 60k current
                .build();

        when(investmentRepository.findByUserIdAndStatus(userId, InvestmentStatus.ACTIVE))
                .thenReturn(List.of(i1, i2));

        PortfolioSummaryDto summary = investmentService.getPortfolioSummary(userId);

        assertEquals(new BigDecimal("150000.00"), summary.getTotalInvested()); // 1.5L
        assertEquals(new BigDecimal("180000.00"), summary.getCurrentValue()); // 1.8L
        assertEquals(new BigDecimal("30000.00"), summary.getWealthGained()); // 30k
        assertEquals(new BigDecimal("20.00"), summary.getOverallReturnsPercentage()); // 30k / 1.5L = 20%
    }
}
