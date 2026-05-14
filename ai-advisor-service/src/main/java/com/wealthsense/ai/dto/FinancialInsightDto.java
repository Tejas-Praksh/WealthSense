package com.wealthsense.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FinancialInsightDto {

    private BigDecimal totalSpendingCurrentMonth;
    private BigDecimal totalSpendingLastMonth;
    private BigDecimal percentageChange;
    
    private Map<String, BigDecimal> topSpendingCategories;
    private String biggestSingleExpense;
    private BigDecimal dailyAverageSpending;
    
    private String aiRecommendation;
    private BigDecimal savingsRate;
    private List<String> goalProgress;
}
