package com.wealthsense.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SipCalculationResponse {
    private BigDecimal totalInvested;
    private BigDecimal estimatedReturns;
    private BigDecimal maturityAmount;
    private BigDecimal wealthGained;
}
