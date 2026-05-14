package com.wealthsense.investment.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SipCalculationRequest {
    @NotNull(message = "Monthly amount is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Monthly amount must be greater than zero")
    private BigDecimal monthlyAmount;

    @NotNull(message = "Years is required")
    @Min(value = 1, message = "Years must be at least 1")
    private Integer years;

    @NotNull(message = "Expected return rate is required")
    @DecimalMin(value = "0.0", message = "Expected return rate must be positive")
    private BigDecimal expectedReturn;
}
