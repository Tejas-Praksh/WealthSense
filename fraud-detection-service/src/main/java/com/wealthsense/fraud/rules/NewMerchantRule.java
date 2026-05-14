package com.wealthsense.fraud.rules;

import com.wealthsense.common.events.TransactionEvent;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * New merchant rule — first-time merchant yields 0.2 risk.
 * Checks if merchant has appeared in recent transaction history.
 */
@Component
public class NewMerchantRule implements FraudRule {

    @Override
    public String getRuleName() {
        return "NEW_MERCHANT";
    }

    @Override
    public double evaluate(TransactionEvent event, List<TransactionEvent> recentTransactions) {
        String merchantName = event.getMerchantName();
        if (merchantName == null || merchantName.isBlank()) return 0.0;
        if (recentTransactions == null || recentTransactions.isEmpty()) return 0.2;

        boolean knownMerchant = recentTransactions.stream()
                .anyMatch(t -> merchantName.equals(t.getMerchantName()));

        return knownMerchant ? 0.0 : 0.2;
    }
}
