package com.wealthsense.decision.kafka;

import com.wealthsense.common.constants.KafkaTopics;
import com.wealthsense.common.events.DecisionEvent;
import com.wealthsense.common.events.NotificationEvent;
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
public class DecisionEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishDecision(DecisionEvent event) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaTopics.DECISION_EVENTS,
                event.getUserId().toString(),
                event);

        addCorrelationId(record, event.getCorrelationId());
        
        kafkaTemplate.send(record);
        log.info("[{}] Published DecisionEvent for txn: {}", 
                event.getCorrelationId(), event.getTransactionId());
    }

    public void publishNotification(NotificationEvent event) {
        ProducerRecord<String, Object> record = new ProducerRecord<>(
                KafkaTopics.NOTIFICATION_EVENTS,
                event.getUserId().toString(), // or recipient/some routing key
                event);

        addCorrelationId(record, event.getCorrelationId());
        
        kafkaTemplate.send(record);
        log.info("[{}] Published NotificationEvent for user: {}", 
                event.getCorrelationId(), event.getUserId());
    }
    
    private void addCorrelationId(ProducerRecord<String, Object> record, String correlationId) {
        if (correlationId != null) {
            record.headers().add(new RecordHeader("correlationId",
                    correlationId.getBytes(StandardCharsets.UTF_8)));
        }
    }
}
