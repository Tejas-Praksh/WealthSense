package com.wealthsense.decision.consumer;

import com.wealthsense.common.constants.KafkaTopics;
import com.wealthsense.common.events.FraudAlertEvent;
import com.wealthsense.decision.service.DecisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class FraudAlertConsumer {

    private final DecisionService decisionService;

    @KafkaListener(
            topics = KafkaTopics.FRAUD_ALERTS,
            groupId = "decision-engine-group"
    )
    public void consume(ConsumerRecord<String, FraudAlertEvent> record, Acknowledgment ack) {
        FraudAlertEvent event = record.value();
        log.info("[{}] Consumed FraudAlertEvent for txn: {}", 
                event.getCorrelationId(), event.getTransactionId());

        try {
            decisionService.processFraudAlert(event);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("[{}] Error processing FraudAlertEvent: {}", 
                    event.getCorrelationId(), e.getMessage());
            // Depending on the dead letter queue config, we either fail explicitly or forward.
            // Spring Kafka DefaultErrorHandler will retry 3 times by our config.
            throw e;
        }
    }
}
