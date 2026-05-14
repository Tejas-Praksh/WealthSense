package com.wealthsense.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthsense.common.events.TransactionEvent;
import com.wealthsense.transaction.domain.OutboxEvent;
import com.wealthsense.transaction.kafka.TransactionEventProducer;
import com.wealthsense.transaction.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

/**
 * Outbox pattern publisher — polls outbox table every 5s,
 * publishes pending events to Kafka, marks as PROCESSED.
 * After 3 failed retries → sends to DLQ and marks FAILED.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxPublisherService {

    private final OutboxRepository outboxRepository;
    private final TransactionEventProducer eventProducer;
    private final ObjectMapper objectMapper;

    @Value("${transaction.outbox.max-retries:3}")
    private int maxRetries;

    @Scheduled(fixedDelayString = "${transaction.outbox.poll-interval-ms:5000}")
    @Transactional
    public void publishPendingEvents() {
        List<OutboxEvent> pendingEvents =
                outboxRepository.findTop100ByStatusOrderByCreatedAtAsc("PENDING");

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Processing {} pending outbox events", pendingEvents.size());

        for (OutboxEvent event : pendingEvents) {
            try {
                TransactionEvent txnEvent = objectMapper.readValue(
                        event.getPayload(), TransactionEvent.class);
                eventProducer.publishTransactionEvent(txnEvent);

                event.setStatus("PROCESSED");
                event.setProcessedAt(Instant.now());
                outboxRepository.save(event);

                log.info("Published outbox event: {} for aggregate: {}",
                        event.getId(), event.getAggregateId());

            } catch (Exception e) {
                log.error("Failed to publish outbox event: {}", event.getId(), e);
                handleFailure(event);
            }
        }
    }

    private void handleFailure(OutboxEvent event) {
        event.setRetryCount(event.getRetryCount() + 1);

        if (event.getRetryCount() >= maxRetries) {
            event.setStatus("FAILED");
            log.warn("Outbox event {} exhausted retries, marking FAILED", event.getId());

            // Attempt to send to DLQ
            try {
                TransactionEvent txnEvent = objectMapper.readValue(
                        event.getPayload(), TransactionEvent.class);
                eventProducer.publishToDeadLetterQueue(txnEvent);
            } catch (Exception dlqEx) {
                log.error("Failed to send to DLQ for event: {}", event.getId(), dlqEx);
            }
        }

        outboxRepository.save(event);
    }
}
