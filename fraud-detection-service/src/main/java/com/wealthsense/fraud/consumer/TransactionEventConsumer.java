package com.wealthsense.fraud.consumer;

import com.wealthsense.common.constants.KafkaTopics;
import com.wealthsense.common.events.TransactionEvent;
import com.wealthsense.fraud.service.FraudDetectionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final FraudDetectionService fraudDetectionService;

    @KafkaListener(
            topics = KafkaTopics.TRANSACTION_EVENTS,
            groupId = "fraud-detection-group",
            concurrency = "3"
    )
    public void onTransactionEvent(
            @Payload TransactionEvent event,
            @Header(value = KafkaHeaders.RECEIVED_KEY, required = false) String key) {

        log.info("[{}] Received transaction event: txn={} amount={} type={}",
                event.getCorrelationId(),
                event.getTransactionId(),
                event.getAmount(),
                event.getType());

        try {
            fraudDetectionService.processTransaction(event);
        } catch (Exception e) {
            log.error("[{}] Fraud check failed for txn: {} — {}",
                    event.getCorrelationId(),
                    event.getTransactionId(),
                    e.getMessage(), e);
            throw e; // Let Kafka error handler retry / send to DLQ
        }
    }
}
