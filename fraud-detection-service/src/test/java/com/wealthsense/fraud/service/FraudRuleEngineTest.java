package com.wealthsense.fraud.service;

import com.wealthsense.common.enums.FraudSeverity;
import com.wealthsense.common.enums.TransactionStatus;
import com.wealthsense.common.enums.TransactionType;
import com.wealthsense.common.events.TransactionEvent;
import com.wealthsense.common.testdata.TestDataBuilder;
import com.wealthsense.fraud.rules.DuplicateTransactionRule;
import com.wealthsense.fraud.rules.FraudRule;
import com.wealthsense.fraud.rules.LargeAmountRule;
import com.wealthsense.fraud.rules.VelocityRule;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * High-level fraud rule + scoring expectations (complements {@link RiskScoringServiceTest}).
 */
class FraudRuleEngineTest {

    private final LargeAmountRule largeAmountRule = new LargeAmountRule();
    private final VelocityRule velocityRule = new VelocityRule();
    private final DuplicateTransactionRule duplicateRule = new DuplicateTransactionRule();

    @Test
    void largeAmountRule_smallAmount_scoreBelowThreshold() {
        TransactionEvent event = TestDataBuilder.transactionEvent(
                BigDecimal.valueOf(10_000), "Shop");
        double score = largeAmountRule.evaluate(event, Collections.emptyList());
        assertTrue(score < 0.3);
    }

    @Test
    void largeAmountRule_hugeAmount_scoreAtLeastHighBand() {
        TransactionEvent event = TestDataBuilder.transactionEvent(
                BigDecimal.valueOf(10_000_001), "Shop");
        double score = largeAmountRule.evaluate(event, Collections.emptyList());
        assertTrue(score >= 0.6);
    }

    @Test
    void riskScoring_smallAmountSingleRule_recommendsApprove() {
        RiskScoringService service = new RiskScoringService(List.of(new LargeAmountRule()));
        TransactionEvent event = TestDataBuilder.transactionEvent(
                BigDecimal.valueOf(1000), "Shop");

        RiskScoringService.RiskResult result =
                service.calculateRisk(event, Collections.emptyList());

        assertEquals(FraudSeverity.LOW, result.severity());
        assertEquals("APPROVE", result.recommendedAction());
    }

    @Test
    void riskScoring_highAverageFromRules_recommendsBlockOrFlag() {
        FraudRule alwaysHigh = new FraudRule() {
            @Override
            public String getRuleName() {
                return "HIGH";
            }

            @Override
            public double evaluate(TransactionEvent event, List<TransactionEvent> recentTransactions) {
                return 0.9;
            }
        };
        RiskScoringService service = new RiskScoringService(List.of(alwaysHigh));
        TransactionEvent event = TestDataBuilder.transactionEvent(BigDecimal.ONE, "X");

        RiskScoringService.RiskResult result =
                service.calculateRisk(event, Collections.emptyList());

        assertEquals(FraudSeverity.CRITICAL, result.severity());
        assertEquals("BLOCK", result.recommendedAction());
    }

    @Test
    void velocityRule_manyRecentTransactions_elevatesScore() {
        List<TransactionEvent> recent = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) {
            TransactionEvent e = TransactionEvent.builder()
                    .transactionId(UUID.randomUUID())
                    .amount(BigDecimal.valueOf(500))
                    .type(TransactionType.DEBIT)
                    .status(TransactionStatus.PENDING)
                    .accountId(UUID.randomUUID())
                    .build();
            e.setUserId(UUID.randomUUID());
            e.setTimestamp(Instant.now().minusSeconds(60L * (i + 1)));
            recent.add(e);
        }
        TransactionEvent current = TestDataBuilder.transactionEvent(BigDecimal.ONE, "Shop");
        double score = velocityRule.evaluate(current, recent);
        assertEquals(0.7, score);
    }

    @Test
    void duplicateRule_matchingRecentTransaction_highScore() {
        TransactionEvent current = TestDataBuilder.transactionEvent(
                BigDecimal.valueOf(5000), "MerchantA");
        TransactionEvent prior = TestDataBuilder.transactionEvent(
                BigDecimal.valueOf(5000), "MerchantA");
        prior.setTimestamp(Instant.now().minusSeconds(30));

        double score = duplicateRule.evaluate(current, List.of(prior));
        assertEquals(0.8, score);
    }
}
