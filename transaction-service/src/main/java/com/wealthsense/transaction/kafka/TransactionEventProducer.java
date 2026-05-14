package com.wealthsense.transaction.kafka;

import com.wealthsense.common.constants.KafkaTopics;
import com.wealthsense.common.events.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

/**
 * Kafka producer for transaction events.
 * Key = userId (same user → same partition → ordered).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishTransactionEvent(TransactionEvent event) {
        String key = event.getUserId().toString();
        String topic = KafkaTopics.TRANSACTION_EVENTS;

        ProducerRecord<String, Object> record = new ProducerRecord<>(topic, key, event);

        // Add correlation ID to Kafka headers for distributed tracing
        if (event.getCorrelationId() != null) {
            record.headers().add(new RecordHeader(
                    "correlationId",
                    event.getCorrelationId().getBytes(StandardCharsets.UTF_8)));
        }

        kafkaTemplate.send(record)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish transaction event: {} for user: {}",
                                event.getTransactionId(), key, ex);
                    } else {
                        log.info("Published transaction event: {} to partition: {} offset: {}",
                                event.getTransactionId(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

    public void publishToDeadLetterQueue(TransactionEvent event) {
        String key = event.getUserId().toString();
        kafkaTemplate.send(KafkaTopics.DLQ_TRANSACTION, key, event);
        log.warn("Sent transaction event to DLQ: {}", event.getTransactionId());
    }
}
