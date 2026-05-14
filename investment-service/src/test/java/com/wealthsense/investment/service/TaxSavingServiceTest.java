package com.wealthsense.investment.service;

import com.wealthsense.investment.domain.Investment;
import com.wealthsense.investment.domain.InvestmentType;
import com.wealthsense.investment.dto.TaxSavingDto;
import com.wealthsense.investment.repository.InvestmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaxSavingServiceTest {

    @Mock
    private InvestmentRepository investmentRepository;

    @InjectMocks
    private TaxSavingService taxSavingService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void calculate_noInvestments_returnsFullLimit() {
        when(investmentRepository.findByUserId(userId)).thenReturn(List.of());

        TaxSavingDto result = taxSavingService.calculateTaxSavings(userId);

        assertEquals(new BigDecimal("150000.00"), result.getRemaining80cLimit());
        assertEquals(new BigDecimal("0.00"), result.getInvested80cThisYear());
    }

    @Test
    void calculate_someInvestments_returnsRemaining() {
        LocalDate currentFy = taxSavingService.getStartOfFinancialYear(LocalDate.now());
        
        Investment elss = Investment.builder()
                .type(InvestmentType.ELSS)
                .amountPaise(new BigDecimal("5000000")) // 50,000 rupees
                .startDate(currentFy.plusDays(10))
                .build();

        Investment lumpsum = Investment.builder()
                .type(InvestmentType.LUMPSUM) // not 80C
                .amountPaise(new BigDecimal("5000000"))
                .startDate(currentFy.plusDays(10))
                .build();

        when(investmentRepository.findByUserId(userId)).thenReturn(List.of(elss, lumpsum));

        TaxSavingDto result = taxSavingService.calculateTaxSavings(userId);

        assertEquals(new BigDecimal("100000.00"), result.getRemaining80cLimit());
        assertEquals(new BigDecimal("50000.00"), result.getInvested80cThisYear());
    }

    @Test
    void calculate_limitReached_returnsZero() {
        LocalDate currentFy = taxSavingService.getStartOfFinancialYear(LocalDate.now());

        Investment ppf = Investment.builder()
                .type(InvestmentType.PPF)
                .amountPaise(new BigDecimal("20000000")) // 200,000 rupees > limit
                .startDate(currentFy.plusDays(10))
                .build();

        when(investmentRepository.findByUserId(userId)).thenReturn(List.of(ppf));

        TaxSavingDto result = taxSavingService.calculateTaxSavings(userId);

        assertEquals(new BigDecimal("0.00"), result.getRemaining80cLimit());
        assertEquals(new BigDecimal("200000.00"), result.getInvested80cThisYear());
    }

    @Test
    void getCurrentFinancialYear_correctYearRange() {
        // Test date in Jan (FY should be previous year)
        LocalDate janDate = LocalDate.of(2025, Month.JANUARY, 15);
        LocalDate fyJan = taxSavingService.getStartOfFinancialYear(janDate);
        assertEquals(LocalDate.of(2024, Month.APRIL, 1), fyJan);

        // Test date in May (FY should be current year)
        LocalDate mayDate = LocalDate.of(2025, Month.MAY, 15);
        LocalDate fyMay = taxSavingService.getStartOfFinancialYear(mayDate);
        assertEquals(LocalDate.of(2025, Month.APRIL, 1), fyMay);
    }
}
