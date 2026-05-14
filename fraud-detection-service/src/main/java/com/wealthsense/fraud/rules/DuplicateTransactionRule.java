package com.wealthsense.fraud.rules;

import com.wealthsense.common.events.TransactionEvent;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * Duplicate transaction rule — same amount + same merchant within 5 minutes: 0.8.
 */
@Component
public class DuplicateTransactionRule implements FraudRule {

    private static final Duration DUPLICATE_WINDOW = Duration.ofMinutes(5);

    @Override
    public String getRuleName() {
        return "DUPLICATE_TRANSACTION";
    }

    @Override
    public double evaluate(TransactionEvent event, List<TransactionEvent> recentTransactions) {
        if (recentTransactions == null || recentTransactions.isEmpty()) return 0.0;
        if (event.getAmount() == null) return 0.0;

        Instant windowStart = Instant.now().minus(DUPLICATE_WINDOW);
        String merchantName = event.getMerchantName();

        boolean duplicateFound = recentTransactions.stream()
                .filter(t -> t.getTimestamp() != null && t.getTimestamp().isAfter(windowStart))
                .anyMatch(t ->
                        event.getAmount().compareTo(t.getAmount()) == 0
                                && merchantName != null
                                && merchantName.equals(t.getMerchantName())
                );

        return duplicateFound ? 0.8 : 0.0;
    }
}
