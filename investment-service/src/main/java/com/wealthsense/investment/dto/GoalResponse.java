package com.wealthsense.investment.dto;

import com.wealthsense.investment.domain.GoalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GoalResponse {
    private UUID id;
    private String name;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private BigDecimal monthlySavingNeeded;
    private LocalDate targetDate;
    private GoalStatus status;
    private Integer priority;
    private BigDecimal progressPercentage;
    private Boolean onTrack;
}
