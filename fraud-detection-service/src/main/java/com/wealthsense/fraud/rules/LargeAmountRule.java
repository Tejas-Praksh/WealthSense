package com.wealthsense.fraud.rules;

import com.wealthsense.common.events.TransactionEvent;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Large amount rule:
 * > 500000 paise (₹5000): 0.3
 * > 5000000 paise (₹50000): 0.6
 * > 10000000 paise (₹1 lakh): 0.8
 */
@Component
public class LargeAmountRule implements FraudRule {

    private static final BigDecimal THRESHOLD_1 = new BigDecimal("500000");
    private static final BigDecimal THRESHOLD_2 = new BigDecimal("5000000");
    private static final BigDecimal THRESHOLD_3 = new BigDecimal("10000000");

    @Override
    public String getRuleName() {
        return "LARGE_AMOUNT";
    }

    @Override
    public double evaluate(TransactionEvent event, List<TransactionEvent> recentTransactions) {
        BigDecimal amount = event.getAmount();
        if (amount == null) return 0.0;

        if (amount.compareTo(THRESHOLD_3) > 0) return 0.8;
        if (amount.compareTo(THRESHOLD_2) > 0) return 0.6;
        if (amount.compareTo(THRESHOLD_1) > 0) return 0.3;

        return 0.0;
    }
}
