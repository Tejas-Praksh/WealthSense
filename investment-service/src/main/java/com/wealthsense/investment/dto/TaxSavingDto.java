package com.wealthsense.investment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaxSavingDto {
    private BigDecimal limit80c;
    private BigDecimal invested80cThisYear;
    private BigDecimal remaining80cLimit;
    private BigDecimal suggestedInvestmentAmount;
    private List<String> suggestedInstruments;
}
