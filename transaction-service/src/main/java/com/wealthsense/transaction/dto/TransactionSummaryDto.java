package com.wealthsense.transaction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionSummaryDto {
    private String yearMonth;
    private BigDecimal totalSpending;
    private BigDecimal totalIncome;
    private Map<String, BigDecimal> spendingByCategory;
    private long transactionCount;
}
