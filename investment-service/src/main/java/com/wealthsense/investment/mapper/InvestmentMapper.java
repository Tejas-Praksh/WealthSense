package com.wealthsense.investment.mapper;

import com.wealthsense.investment.domain.Goal;
import com.wealthsense.investment.dto.GoalResponse;
import com.wealthsense.common.util.MoneyUtil;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class InvestmentMapper {

    public GoalResponse toGoalResponse(Goal goal) {
        if (goal == null) {
            return null;
        }

        return GoalResponse.builder()
                .id(goal.getId())
                .name(goal.getName())
                .targetAmount(MoneyUtil.paiseToRupees(goal.getTargetAmountPaise()))
                .currentAmount(MoneyUtil.paiseToRupees(goal.getCurrentAmountPaise()))
                .monthlySavingNeeded(MoneyUtil.paiseToRupees(goal.getMonthlySavingPaise()))
                .targetDate(goal.getTargetDate())
                .status(goal.getStatus())
                .priority(goal.getPriority())
                .progressPercentage(BigDecimal.ZERO) // Calculated in Service layer
                .onTrack(Boolean.FALSE) // Calculated in Service layer
                .build();
    }
}
