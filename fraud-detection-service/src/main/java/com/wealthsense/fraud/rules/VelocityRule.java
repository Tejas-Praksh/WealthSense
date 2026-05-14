package com.wealthsense.fraud.rules;

import com.wealthsense.common.events.TransactionEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Velocity rule — checks transaction frequency:
 * > 10 txns in 1 hour: 0.7
 * > 5 txns in 1 hour: 0.4
 */
@Component
public class VelocityRule implements FraudRule {

    private static final Duration WINDOW = Duration.ofHours(1);

    @Override
    public String getRuleName() {
        return "VELOCITY";
    }

    @Override
    public double evaluate(TransactionEvent event, List<TransactionEvent> recentTransactions) {
        if (recentTransactions == null || recentTransactions.isEmpty()) return 0.0;

        Instant windowStart = Instant.now().minus(WINDOW);

        long count = recentTransactions.stream()
                .filter(t -> t.getTimestamp() != null && t.getTimestamp().isAfter(windowStart))
                .count();

        if (count > 10) return 0.7;
        if (count > 5) return 0.4;
        return 0.0;
    }
}
