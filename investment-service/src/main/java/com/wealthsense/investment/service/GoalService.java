package com.wealthsense.investment.service;

import com.wealthsense.investment.domain.Goal;
import com.wealthsense.investment.dto.GoalRequest;
import com.wealthsense.investment.dto.GoalResponse;
import com.wealthsense.investment.mapper.InvestmentMapper;
import com.wealthsense.investment.repository.GoalRepository;
import com.wealthsense.common.exception.WealthSenseException;
import com.wealthsense.common.util.MoneyUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GoalService {

    private final GoalRepository goalRepository;
    private final InvestmentMapper goalMapper;

    public GoalResponse createGoal(UUID userId, GoalRequest request) {
        long monthsToGoal = ChronoUnit.MONTHS.between(LocalDate.now(), request.getTargetDate());
        if (monthsToGoal <= 0) {
            monthsToGoal = 1; // Minimum 1 month
        }

        BigDecimal targetPaise = MoneyUtil.rupeesToPaise(request.getTargetAmount());
        BigDecimal monthlySavingPaise = targetPaise.divide(BigDecimal.valueOf(monthsToGoal), 0, RoundingMode.UP);

        Goal goal = Goal.builder()
                .userId(userId)
                .name(request.getName())
                .targetAmountPaise(targetPaise)
                .currentAmountPaise(BigDecimal.ZERO)
                .monthlySavingPaise(monthlySavingPaise)
                .targetDate(request.getTargetDate())
                .status(com.wealthsense.investment.domain.GoalStatus.ACTIVE)
                .priority(request.getPriority() != null ? request.getPriority() : 1)
                .build();

        Goal savedGoal = goalRepository.save(goal);
        return enhanceGoalResponse(goalMapper.toGoalResponse(savedGoal), savedGoal);
    }

    public List<GoalResponse> getGoals(UUID userId) {
        return goalRepository.findByUserId(userId).stream()
                .map(goal -> enhanceGoalResponse(goalMapper.toGoalResponse(goal), goal))
                .collect(Collectors.toList());
    }

    public GoalResponse updateGoal(UUID userId, UUID goalId, GoalRequest request) {
        Goal goal = goalRepository.findById(goalId)
                .orElseThrow(() -> new WealthSenseException("GOAL_NOT_FOUND", "Goal not found", HttpStatus.NOT_FOUND));

        if (!goal.getUserId().equals(userId)) {
            throw new WealthSenseException("FORBIDDEN", "Not authorized", HttpStatus.FORBIDDEN);
        }

        goal.setTargetDate(request.getTargetDate());
        goal.setTargetAmountPaise(MoneyUtil.rupeesToPaise(request.getTargetAmount()));
        
        long monthsToGoal = ChronoUnit.MONTHS.between(LocalDate.now(), request.getTargetDate());
        if (monthsToGoal <= 0) monthsToGoal = 1;
        
        BigDecimal remainingPaise = goal.getTargetAmountPaise().subtract(goal.getCurrentAmountPaise());
        if (remainingPaise.compareTo(BigDecimal.ZERO) < 0) remainingPaise = BigDecimal.ZERO;

        BigDecimal monthlySavingPaise = remainingPaise.divide(BigDecimal.valueOf(monthsToGoal), 0, RoundingMode.UP);
        goal.setMonthlySavingPaise(monthlySavingPaise);

        Goal savedGoal = goalRepository.save(goal);
        return enhanceGoalResponse(goalMapper.toGoalResponse(savedGoal), savedGoal);
    }

    private GoalResponse enhanceGoalResponse(GoalResponse response, Goal goal) {
        if (goal.getTargetAmountPaise().compareTo(BigDecimal.ZERO) == 0) {
            response.setProgressPercentage(BigDecimal.ZERO);
            response.setOnTrack(true);
            return response;
        }

        BigDecimal progress = goal.getCurrentAmountPaise()
                .divide(goal.getTargetAmountPaise(), 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.HALF_UP);
        response.setProgressPercentage(progress);

        LocalDate startDate = goal.getCreatedAt() != null ? LocalDate.from(goal.getCreatedAt()) : LocalDate.now();
        long monthsElapsed = ChronoUnit.MONTHS.between(startDate, LocalDate.now());
        if (monthsElapsed < 0) monthsElapsed = 0;
        
        BigDecimal expectedAmountPaise = goal.getMonthlySavingPaise().multiply(BigDecimal.valueOf(monthsElapsed));
        boolean onTrack = goal.getCurrentAmountPaise().compareTo(expectedAmountPaise) >= 0;
        response.setOnTrack(onTrack);

        return response;
    }
}
