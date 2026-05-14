package com.wealthsense.fraud.rules;

import com.wealthsense.common.events.TransactionEvent;

import java.util.List;

/**
 * Strategy interface for fraud detection rules.
 * Each rule evaluates a transaction and returns a risk contribution (0.0 to 1.0).
 */
public interface FraudRule {

    String getRuleName();

    /**
     * @param event   the transaction event to evaluate
     * @param recentTransactions recent transactions for velocity/duplicate checks (may be empty)
     * @return risk contribution from 0.0 (safe) to 1.0 (definite fraud)
     */
    double evaluate(TransactionEvent event, List<TransactionEvent> recentTransactions);
}
