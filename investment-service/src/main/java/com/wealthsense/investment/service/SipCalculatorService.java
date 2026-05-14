package com.wealthsense.investment.service;

import com.wealthsense.investment.dto.SipCalculationRequest;
import com.wealthsense.investment.dto.SipCalculationResponse;
import com.wealthsense.common.exception.WealthSenseException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class SipCalculatorService {

    public SipCalculationResponse calculate(SipCalculationRequest request) {
        if (request.getYears() <= 0) {
            throw new WealthSenseException(
                    "INVALID_YEARS",
                    "Years must be greater than zero",
                    HttpStatus.BAD_REQUEST
            );
        }

        BigDecimal p = request.getMonthlyAmount();
        int n = request.getYears() * 12;
        BigDecimal expectedReturn = request.getExpectedReturn();

        BigDecimal totalInvested = p.multiply(BigDecimal.valueOf(n));

        if (expectedReturn.compareTo(BigDecimal.ZERO) == 0) {
            return SipCalculationResponse.builder()
                    .totalInvested(totalInvested.setScale(2, RoundingMode.HALF_UP))
                    .maturityAmount(totalInvested.setScale(2, RoundingMode.HALF_UP))
                    .estimatedReturns(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .wealthGained(BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP))
                    .build();
        }

        // r = annualRate / 12 / 100
        BigDecimal annualRate = expectedReturn.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
        BigDecimal r = annualRate.divide(BigDecimal.valueOf(12), 10, RoundingMode.HALF_UP);

        // FV = P * [ ((1+r)^n - 1) / r ] * (1+r)
        BigDecimal onePlusR = BigDecimal.ONE.add(r);
        BigDecimal onePlusRPowN = onePlusR.pow(n);
        
        BigDecimal maturityAmount = p.multiply(onePlusRPowN.subtract(BigDecimal.ONE))
                .divide(r, 10, RoundingMode.HALF_UP)
                .multiply(onePlusR)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal estimatedReturns = maturityAmount.subtract(totalInvested);
        BigDecimal wealthGained = BigDecimal.ZERO;
        if(totalInvested.compareTo(BigDecimal.ZERO) > 0) {
            wealthGained = estimatedReturns.divide(totalInvested, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        }

        return SipCalculationResponse.builder()
                .totalInvested(totalInvested.setScale(2, RoundingMode.HALF_UP))
                .estimatedReturns(estimatedReturns.setScale(2, RoundingMode.HALF_UP))
                .maturityAmount(maturityAmount)
                .wealthGained(wealthGained)
                .build();
    }
}
