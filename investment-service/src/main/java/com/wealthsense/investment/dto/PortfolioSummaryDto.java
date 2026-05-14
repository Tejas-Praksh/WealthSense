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
public class PortfolioSummaryDto {
    private BigDecimal totalInvested;
    private BigDecimal currentValue;
    private BigDecimal overallReturnsPercentage;
    private BigDecimal wealthGained;
}
