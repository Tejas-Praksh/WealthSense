package com.wealthsense.fraud.kafka;

import com.wealthsense.common.constants.KafkaTopics;
import com.wealthsense.common.events.FraudAlertEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
@RequiredArgsConstructor
public class FraudAlertProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishFraudAlert(FraudAlertEvent event) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaTopics.FRAUD_ALERTS,
                event.getUserId().toString(),
                event);

        if (event.getCorrelationId() != null) {
            record.headers().add(new RecordHeader("correlationId",
                    event.getCorrelationId().getBytes(StandardCharsets.UTF_8)));
        }

        kafkaTemplate.send(record);
        log.info("[{}] Published fraud alert: txn={} severity={} action={}",
                event.getCorrelationId(),
                event.getTransactionId(),
                event.getSeverity(),
                event.getRecommendedAction());
    }
}
