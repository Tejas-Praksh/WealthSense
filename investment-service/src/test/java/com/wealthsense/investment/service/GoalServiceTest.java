package com.wealthsense.investment.service;

import com.wealthsense.investment.domain.Goal;
import com.wealthsense.investment.domain.GoalStatus;
import com.wealthsense.investment.dto.GoalRequest;
import com.wealthsense.investment.dto.GoalResponse;
import com.wealthsense.investment.mapper.InvestmentMapper;
import com.wealthsense.investment.repository.GoalRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoalServiceTest {

    @Mock
    private GoalRepository goalRepository;

    @Mock
    private InvestmentMapper goalMapper;

    @InjectMocks
    private GoalService goalService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
    }

    @Test
    void createGoal_validRequest_calculatesMonthly() {
        GoalRequest req = GoalRequest.builder()
                .name("Car")
                .targetAmount(new BigDecimal("500000")) // 5 lakhs
                .targetDate(LocalDate.now().plusMonths(10))
                .build();

        Goal savedGoal = Goal.builder()
                .id(UUID.randomUUID())
                .targetAmountPaise(new BigDecimal("50000000")) // converted
                .currentAmountPaise(BigDecimal.ZERO)
                .monthlySavingPaise(new BigDecimal("5000000"))
                .createdAt(LocalDateTime.now())
                .build();

        when(goalRepository.save(any())).thenReturn(savedGoal);
        when(goalMapper.toGoalResponse(savedGoal)).thenReturn(GoalResponse.builder().build());

        GoalResponse response = goalService.createGoal(userId, req);

        assertNotNull(response);
        verify(goalRepository, times(1)).save(any(Goal.class));
    }

    @Test
    void getGoals_withProgress_returnsCorrectPercentage() {
        Goal goal = Goal.builder()
                .id(UUID.randomUUID())
                .targetAmountPaise(new BigDecimal("10000000")) // 1 Lakh
                .currentAmountPaise(new BigDecimal("5000000"))  // 50k
                .monthlySavingPaise(new BigDecimal("1000000"))  // 10k/mo
                .createdAt(LocalDateTime.now().minusMonths(5))
                .build();

        when(goalRepository.findByUserId(userId)).thenReturn(List.of(goal));
        when(goalMapper.toGoalResponse(any())).thenReturn(GoalResponse.builder().build());

        List<GoalResponse> responses = goalService.getGoals(userId);

        assertEquals(1, responses.size());
        assertEquals(new BigDecimal("50.00"), responses.get(0).getProgressPercentage());
    }

    @Test
    void isOnTrack_behindSchedule_returnsFalse() {
        // Goal requires 10k/mo, 5 months elapsed = 50k expected. Currently has 20k.
        Goal goal = Goal.builder()
                .id(UUID.randomUUID())
                .targetAmountPaise(new BigDecimal("10000000"))
                .currentAmountPaise(new BigDecimal("2000000"))
                .monthlySavingPaise(new BigDecimal("1000000"))
                .createdAt(LocalDateTime.now().minusMonths(5))
                .build();

        when(goalRepository.findByUserId(userId)).thenReturn(List.of(goal));
        when(goalMapper.toGoalResponse(any())).thenReturn(GoalResponse.builder().build());

        List<GoalResponse> responses = goalService.getGoals(userId);

        assertFalse(responses.get(0).getOnTrack());
    }

    @Test
    void isOnTrack_onSchedule_returnsTrue() {
        // Goal requires 10k/mo, 5 months elapsed = 50k expected. Currently has 60k.
        Goal goal = Goal.builder()
                .id(UUID.randomUUID())
                .targetAmountPaise(new BigDecimal("10000000"))
                .currentAmountPaise(new BigDecimal("6000000"))
                .monthlySavingPaise(new BigDecimal("1000000"))
                .createdAt(LocalDateTime.now().minusMonths(5))
                .build();

        when(goalRepository.findByUserId(userId)).thenReturn(List.of(goal));
        when(goalMapper.toGoalResponse(any())).thenReturn(GoalResponse.builder().build());

        List<GoalResponse> responses = goalService.getGoals(userId);

        assertTrue(responses.get(0).getOnTrack());
    }
}
