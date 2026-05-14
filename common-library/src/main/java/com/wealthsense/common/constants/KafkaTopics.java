package com.wealthsense.common.constants;

public final class KafkaTopics {

    private KafkaTopics() {
        throw new UnsupportedOperationException("Constants class — do not instantiate");
    }

    public static final String TRANSACTION_EVENTS = "transaction-events";
    public static final String FRAUD_ALERTS = "fraud-alerts";
    public static final String DECISION_EVENTS = "decision-events";
    public static final String NOTIFICATION_EVENTS = "notification-events";
    public static final String AUDIT_EVENTS = "audit-events";
    public static final String OUTBOX_EVENTS = "outbox-events";

    // Dead Letter Queues
    public static final String DLQ_TRANSACTION = "transaction-events.DLQ";
    public static final String DLQ_FRAUD = "fraud-alerts.DLQ";
    public static final String DLQ_NOTIFICATION = "notification-events.DLQ";
}
