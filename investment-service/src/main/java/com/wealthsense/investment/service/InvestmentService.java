package com.wealthsense.investment.service;

import com.wealthsense.investment.domain.Investment;
import com.wealthsense.investment.domain.InvestmentStatus;
import com.wealthsense.investment.dto.PortfolioSummaryDto;
import com.wealthsense.investment.repository.InvestmentRepository;
import com.wealthsense.common.util.MoneyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class InvestmentService {

    private final InvestmentRepository investmentRepository;

    @Cacheable(value = "investment-recommendations", key = "#userId", unless = "#result == null")
    public List<String> getRecommendations(UUID userId, BigDecimal monthlyIncomeRupees) {
        if (monthlyIncomeRupees == null) {
            return List.of("Conservative (FD, PPF)");
        }
        if (monthlyIncomeRupees.compareTo(new BigDecimal("50000")) < 0) {
            return List.of("Conservative (FD, PPF)"); // low risk
        } else if (monthlyIncomeRupees.compareTo(new BigDecimal("150000")) < 0) {
            return List.of("Moderate (Balanced MF)"); // medium risk
        } else {
            return List.of("Aggressive (ELSS, Index)"); // high risk
        }
    }

    public PortfolioSummaryDto getPortfolioSummary(UUID userId) {
        List<Investment> investments = investmentRepository.findByUserIdAndStatus(userId, InvestmentStatus.ACTIVE);

        BigDecimal totalInvestedPaise = investments.stream()
                .map(inv -> inv.getAmountPaise() != null ? inv.getAmountPaise() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal currentValuePaise = investments.stream()
                .map(inv -> inv.getCurrentValuePaise() != null ? inv.getCurrentValuePaise() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal wealthGainedPaise = currentValuePaise.subtract(totalInvestedPaise);

        BigDecimal overallReturnsPercentage = BigDecimal.ZERO;
        if (totalInvestedPaise.compareTo(BigDecimal.ZERO) > 0) {
            overallReturnsPercentage = wealthGainedPaise
                    .divide(totalInvestedPaise, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        return PortfolioSummaryDto.builder()
                .totalInvested(MoneyUtil.paiseToRupees(totalInvestedPaise))
                .currentValue(MoneyUtil.paiseToRupees(currentValuePaise))
                .wealthGained(MoneyUtil.paiseToRupees(wealthGainedPaise))
                .overallReturnsPercentage(overallReturnsPercentage)
                .build();
    }
}
