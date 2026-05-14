package com.wealthsense.fraud.rules;

import com.wealthsense.common.events.TransactionEvent;
import com.wealthsense.common.util.DateTimeUtil;
import org.springframework.stereotype.Component;

import java.time.ZonedDateTime;
import java.util.List;

/**
 * Unusual time rule — transactions between 1 AM - 5 AM IST score 0.3.
 */
@Component
public class UnusualTimeRule implements FraudRule {

    @Override
    public String getRuleName() {
        return "UNUSUAL_TIME";
    }

    @Override
    public double evaluate(TransactionEvent event, List<TransactionEvent> recentTransactions) {
        if (event.getTimestamp() == null) return 0.0;

        ZonedDateTime ist = DateTimeUtil.toIst(event.getTimestamp());
        int hour = ist.getHour();

        // 1 AM to 5 AM IST is suspicious
        if (hour >= 1 && hour < 5) {
            return 0.3;
        }

        return 0.0;
    }
}
