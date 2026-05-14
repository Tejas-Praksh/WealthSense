package com.wealthsense.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthsense.transaction.domain.OutboxEvent;
import com.wealthsense.transaction.kafka.TransactionEventProducer;
import com.wealthsense.transaction.repository.OutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxPublisherServiceTest {

    @Mock private OutboxRepository outboxRepository;
    @Mock private TransactionEventProducer eventProducer;
    @Spy  private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private OutboxPublisherService outboxPublisherService;

    private String validPayload() {
        return """
                {"transactionId":"11111111-1111-1111-1111-111111111111",
                 "userId":"22222222-2222-2222-2222-222222222222",
                 "amount":5000,"currency":"INR",
                 "type":"DEBIT","status":"PENDING",
                 "accountId":"33333333-3333-3333-3333-333333333333"}""";
    }

    @Test
    void publishPendingEvents_successfulKafka_marksProcessed() {
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .eventType("TRANSACTION_CREATED")
                .payload(validPayload())
                .topic("transaction-events")
                .status("PENDING")
                .build();

        when(outboxRepository.findTop100ByStatusOrderByCreatedAtAsc("PENDING"))
                .thenReturn(List.of(event));
        doNothing().when(eventProducer).publishTransactionEvent(any());

        outboxPublisherService.publishPendingEvents();

        assertEquals("PROCESSED", event.getStatus());
        verify(outboxRepository).save(event);
        verify(eventProducer).publishTransactionEvent(any());
    }

    @Test
    void publishPendingEvents_kafkaFailure_incrementsRetry() {
        ReflectionTestUtils.setField(outboxPublisherService, "maxRetries", 3);
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .eventType("TRANSACTION_CREATED")
                .payload(validPayload())
                .topic("transaction-events")
                .status("PENDING")
                .retryCount(0)
                .build();

        when(outboxRepository.findTop100ByStatusOrderByCreatedAtAsc("PENDING"))
                .thenReturn(List.of(event));
        doThrow(new RuntimeException("Kafka down"))
                .when(eventProducer).publishTransactionEvent(any());

        outboxPublisherService.publishPendingEvents();

        assertEquals(1, event.getRetryCount());
        assertEquals("PENDING", event.getStatus()); // Still pending (retry < max)
        verify(outboxRepository).save(event);
    }

    @Test
    void publishPendingEvents_3failures_sendsToDeadLetterQueue() {
        ReflectionTestUtils.setField(outboxPublisherService, "maxRetries", 3);
        OutboxEvent event = OutboxEvent.builder()
                .id(UUID.randomUUID())
                .aggregateId(UUID.randomUUID())
                .eventType("TRANSACTION_CREATED")
                .payload(validPayload())
                .topic("transaction-events")
                .status("PENDING")
                .retryCount(2) // Already retried twice
                .build();

        when(outboxRepository.findTop100ByStatusOrderByCreatedAtAsc("PENDING"))
                .thenReturn(List.of(event));
        doThrow(new RuntimeException("Kafka down"))
                .when(eventProducer).publishTransactionEvent(any());

        outboxPublisherService.publishPendingEvents();

        assertEquals("FAILED", event.getStatus());
        assertEquals(3, event.getRetryCount());
        verify(eventProducer).publishToDeadLetterQueue(any());
    }

    @Test
    void publishPendingEvents_noEvents_doesNothing() {
        when(outboxRepository.findTop100ByStatusOrderByCreatedAtAsc("PENDING"))
                .thenReturn(Collections.emptyList());

        outboxPublisherService.publishPendingEvents();

        verify(eventProducer, never()).publishTransactionEvent(any());
    }
}
