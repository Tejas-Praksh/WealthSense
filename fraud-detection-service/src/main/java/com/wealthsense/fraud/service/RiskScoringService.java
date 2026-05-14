package com.wealthsense.fraud.service;

import com.wealthsense.common.enums.FraudSeverity;
import com.wealthsense.common.events.TransactionEvent;
import com.wealthsense.fraud.rules.FraudRule;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Runs ALL fraud rules in parallel via CompletableFuture.
 * Final score = average of all rule scores.
 * If any single rule > 0.8 → immediate CRITICAL.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RiskScoringService {

    private final List<FraudRule> rules;

    public RiskResult calculateRisk(TransactionEvent event,
                                    List<TransactionEvent> recentTransactions) {
        // Run all rules in parallel
        List<CompletableFuture<RuleScore>> futures = rules.stream()
                .map(rule -> CompletableFuture.supplyAsync(() -> {
                    double score = rule.evaluate(event, recentTransactions);
                    return new RuleScore(rule.getRuleName(), score);
                }))
                .toList();

        // Wait for all rules to complete
        List<RuleScore> scores = futures.stream()
                .map(CompletableFuture::join)
                .toList();

        // Identify critical rules (any single rule > 0.8)
        boolean hasCritical = scores.stream().anyMatch(s -> s.score() > 0.8);

        // Final score = average
        double finalScore = scores.stream()
                .mapToDouble(RuleScore::score)
                .average()
                .orElse(0.0);

        // Determine triggered rules
        String triggeredRules = scores.stream()
                .filter(s -> s.score() > 0.0)
                .map(s -> s.ruleName() + "=" + String.format("%.2f", s.score()))
                .collect(Collectors.joining(", "));

        // Build evidence map
        Map<String, Object> evidence = scores.stream()
                .collect(Collectors.toMap(RuleScore::ruleName, RuleScore::score));

        // Determine severity
        FraudSeverity severity;
        String action;
        if (hasCritical || finalScore >= 0.8) {
            severity = FraudSeverity.CRITICAL;
            action = "BLOCK";
        } else if (finalScore >= 0.6) {
            severity = FraudSeverity.HIGH;
            action = "FLAG";
        } else if (finalScore >= 0.4) {
            severity = FraudSeverity.MEDIUM;
            action = "REVIEW";
        } else {
            severity = FraudSeverity.LOW;
            action = "APPROVE";
        }

        log.debug("Transaction {} risk score: {} severity: {} triggered: {}",
                event.getTransactionId(), String.format("%.4f", finalScore),
                severity, triggeredRules);

        return new RiskResult(finalScore, severity, action,
                triggeredRules, evidence);
    }

    public record RuleScore(String ruleName, double score) {}

    public record RiskResult(
            double riskScore,
            FraudSeverity severity,
            String recommendedAction,
            String triggeredRules,
            Map<String, Object> evidence
    ) {}
}
