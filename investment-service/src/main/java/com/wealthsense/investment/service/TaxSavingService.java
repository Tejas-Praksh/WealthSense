package com.wealthsense.investment.service;

import com.wealthsense.investment.domain.Investment;
import com.wealthsense.investment.domain.InvestmentType;
import com.wealthsense.investment.dto.TaxSavingDto;
import com.wealthsense.investment.repository.InvestmentRepository;
import com.wealthsense.common.util.MoneyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaxSavingService {

    private static final BigDecimal LIMIT_80C_RUPEES = new BigDecimal("150000.00");
    private static final BigDecimal LIMIT_80C_PAISE = MoneyUtil.rupeesToPaise(LIMIT_80C_RUPEES);

    private final InvestmentRepository investmentRepository;

    public TaxSavingDto calculateTaxSavings(UUID userId) {
        List<Investment> userInvestments = investmentRepository.findByUserId(userId);

        LocalDate now = LocalDate.now();
        LocalDate startOfFy = getStartOfFinancialYear(now);
        LocalDate endOfFy = startOfFy.plusYears(1).minusDays(1);

        BigDecimal invested80cThisYearPaise = userInvestments.stream()
                .filter(inv -> is80cInstrument(inv.getType()))
                .filter(inv -> inv.getStartDate() != null && !inv.getStartDate().isBefore(startOfFy) && !inv.getStartDate().isAfter(endOfFy))
                .map(inv -> inv.getAmountPaise() != null ? inv.getAmountPaise() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remainingPaise = LIMIT_80C_PAISE.subtract(invested80cThisYearPaise);
        if (remainingPaise.compareTo(BigDecimal.ZERO) < 0) {
            remainingPaise = BigDecimal.ZERO;
        }

        return TaxSavingDto.builder()
                .limit80c(LIMIT_80C_RUPEES)
                .invested80cThisYear(MoneyUtil.paiseToRupees(invested80cThisYearPaise))
                .remaining80cLimit(MoneyUtil.paiseToRupees(remainingPaise))
                .suggestedInvestmentAmount(MoneyUtil.paiseToRupees(remainingPaise))
                .suggestedInstruments(List.of("ELSS Mutual Funds", "PPF (Public Provident Fund)", "NPS (National Pension System)"))
                .build();
    }

    protected LocalDate getStartOfFinancialYear(LocalDate date) {
        if (date.getMonthValue() < Month.APRIL.getValue()) {
            return LocalDate.of(date.getYear() - 1, Month.APRIL, 1);
        } else {
            return LocalDate.of(date.getYear(), Month.APRIL, 1);
        }
    }

    private boolean is80cInstrument(InvestmentType type) {
        return type == InvestmentType.ELSS || type == InvestmentType.PPF;
    }
}
