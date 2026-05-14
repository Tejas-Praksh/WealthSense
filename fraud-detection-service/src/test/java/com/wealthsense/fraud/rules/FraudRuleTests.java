package com.wealthsense.fraud.rules;

import com.wealthsense.common.enums.TransactionStatus;
import com.wealthsense.common.enums.TransactionType;
import com.wealthsense.common.events.TransactionEvent;
import com.wealthsense.common.testdata.TestDataBuilder;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class FraudRuleTests {

    // ========== LargeAmountRule ==========

    private final LargeAmountRule largeAmountRule = new LargeAmountRule();

    @Test
    void largeAmountRule_smallAmount_returnsLowScore() {
        TransactionEvent event = buildEvent(new BigDecimal("1000"), null);
        double score = largeAmountRule.evaluate(event, Collections.emptyList());
        assertEquals(0.0, score);
    }

    @Test
    void largeAmountRule_largeAmount_returnsHighScore() {
        TransactionEvent event = buildEvent(new BigDecimal("10000001"), null);
        double score = largeAmountRule.evaluate(event, Collections.emptyList());
        assertEquals(0.8, score);
    }

    @Test
    void largeAmountRule_mediumAmount_returnsMediumScore() {
        TransactionEvent event = buildEvent(new BigDecimal("5000001"), null);
        double score = largeAmountRule.evaluate(event, Collections.emptyList());
        assertEquals(0.6, score);
    }

    @Test
    void largeAmountRule_threshold1_returnsScore03() {
        TransactionEvent event = buildEvent(new BigDecimal("500001"), null);
        double score = largeAmountRule.evaluate(event, Collections.emptyList());
        assertEquals(0.3, score);
    }

    // ========== VelocityRule ==========

    private final VelocityRule velocityRule = new VelocityRule();

    @Test
    void velocityRule_fewTransactions_returnsLowScore() {
        List<TransactionEvent> recent = List.of(
                buildRecentEvent(Instant.now().minusSeconds(120)),
                buildRecentEvent(Instant.now().minusSeconds(300))
        );
        double score = velocityRule.evaluate(buildEvent(new BigDecimal("1000"), null), recent);
        assertEquals(0.0, score);
    }

    @Test
    void velocityRule_manyTransactions_returnsHighScore() {
        // 11 transactions in the last 30 minutes
        List<TransactionEvent> recent = new java.util.ArrayList<>();
        for (int i = 0; i < 11; i++) {
            recent.add(buildRecentEvent(Instant.now().minusSeconds(60 * (i + 1))));
        }
        double score = velocityRule.evaluate(buildEvent(new BigDecimal("1000"), null), recent);
        assertEquals(0.7, score);
    }

    @Test
    void velocityRule_mediumVelocity_returnsMediumScore() {
        List<TransactionEvent> recent = new java.util.ArrayList<>();
        for (int i = 0; i < 6; i++) {
            recent.add(buildRecentEvent(Instant.now().minusSeconds(60 * (i + 1))));
        }
        double score = velocityRule.evaluate(buildEvent(new BigDecimal("1000"), null), recent);
        assertEquals(0.4, score);
    }

    // ========== DuplicateTransactionRule ==========

    private final DuplicateTransactionRule duplicateRule = new DuplicateTransactionRule();

    @Test
    void duplicateRule_sameAmountSameMerchant_returnsHighScore() {
        TransactionEvent event = buildEvent(new BigDecimal("5000"), "MerchantA");
        TransactionEvent recentDup = buildEvent(new BigDecimal("5000"), "MerchantA");
        recentDup.setTimestamp(Instant.now().minusSeconds(60)); // 1 min ago

        double score = duplicateRule.evaluate(event, List.of(recentDup));
        assertEquals(0.8, score);
    }

    @Test
    void duplicateRule_differentMerchant_returnsLowScore() {
        TransactionEvent event = buildEvent(new BigDecimal("5000"), "MerchantA");
        TransactionEvent recent = buildEvent(new BigDecimal("5000"), "MerchantB");
        recent.setTimestamp(Instant.now().minusSeconds(60));

        double score = duplicateRule.evaluate(event, List.of(recent));
        assertEquals(0.0, score);
    }

    // ========== UnusualTimeRule ==========

    private final UnusualTimeRule unusualTimeRule = new UnusualTimeRule();

    @Test
    void unusualTimeRule_normalHours_returnsLowScore() {
        // 10 AM IST = 4:30 AM UTC
        ZonedDateTime normalTime = ZonedDateTime.of(2026, 5, 6, 10, 0, 0, 0,
                ZoneId.of("Asia/Kolkata"));
        TransactionEvent event = buildEvent(new BigDecimal("1000"), null);
        event.setTimestamp(normalTime.toInstant());

        double score = unusualTimeRule.evaluate(event, Collections.emptyList());
        assertEquals(0.0, score);
    }

    @Test
    void unusualTimeRule_lateNight_returnsHighScore() {
        // 2 AM IST = 8:30 PM UTC previous day
        ZonedDateTime lateNight = ZonedDateTime.of(2026, 5, 6, 2, 0, 0, 0,
                ZoneId.of("Asia/Kolkata"));
        TransactionEvent event = buildEvent(new BigDecimal("1000"), null);
        event.setTimestamp(lateNight.toInstant());

        double score = unusualTimeRule.evaluate(event, Collections.emptyList());
        assertEquals(0.3, score);
    }

    // ========== NewMerchantRule ==========

    private final NewMerchantRule newMerchantRule = new NewMerchantRule();

    @Test
    void newMerchantRule_knownMerchant_returnsLowScore() {
        TransactionEvent event = buildEvent(new BigDecimal("1000"), "KnownShop");
        TransactionEvent past = buildEvent(new BigDecimal("2000"), "KnownShop");

        double score = newMerchantRule.evaluate(event, List.of(past));
        assertEquals(0.0, score);
    }

    @Test
    void newMerchantRule_unknownMerchant_returnsScore() {
        TransactionEvent event = buildEvent(new BigDecimal("1000"), "BrandNewShop");
        TransactionEvent past = buildEvent(new BigDecimal("2000"), "OldShop");

        double score = newMerchantRule.evaluate(event, List.of(past));
        assertEquals(0.2, score);
    }

    @Test
    void newMerchantRule_noHistory_returnsScore() {
        TransactionEvent event = buildEvent(new BigDecimal("1000"), "SomeShop");
        double score = newMerchantRule.evaluate(event, Collections.emptyList());
        assertEquals(0.2, score);
    }

    // ========== Helpers ==========

    private TransactionEvent buildEvent(BigDecimal amount, String merchantName) {
        return TestDataBuilder.transactionEvent(amount,
                merchantName != null ? merchantName : "Merchant");
    }

    private TransactionEvent buildRecentEvent(Instant timestamp) {
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(UUID.randomUUID())
                .amount(new BigDecimal("1000"))
                .type(TransactionType.DEBIT)
                .status(TransactionStatus.PENDING)
                .accountId(UUID.randomUUID())
                .build();
        event.setUserId(UUID.randomUUID());
        event.setTimestamp(timestamp);
        return event;
    }
}
