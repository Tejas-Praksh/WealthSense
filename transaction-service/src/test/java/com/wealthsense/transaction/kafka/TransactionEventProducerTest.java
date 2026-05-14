package com.wealthsense.transaction.kafka;

import com.wealthsense.common.constants.KafkaTopics;
import com.wealthsense.common.enums.TransactionStatus;
import com.wealthsense.common.enums.TransactionType;
import com.wealthsense.common.events.TransactionEvent;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionEventProducerTest {

    @Mock private KafkaTemplate<String, Object> kafkaTemplate;

    @InjectMocks
    private TransactionEventProducer producer;

    @Test
    void publish_validEvent_sendsToCorrectTopic() {
        UUID userId = UUID.randomUUID();
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(UUID.randomUUID())
                .amount(new BigDecimal("5000"))
                .type(TransactionType.DEBIT)
                .status(TransactionStatus.PENDING)
                .accountId(UUID.randomUUID())
                .build();
        event.setUserId(userId);
        event.setCorrelationId("corr-test-123");

        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(new CompletableFuture<>());

        producer.publishTransactionEvent(event);

        ArgumentCaptor<ProducerRecord<String, Object>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate).send(captor.capture());

        ProducerRecord<String, Object> record = captor.getValue();
        assertEquals(KafkaTopics.TRANSACTION_EVENTS, record.topic());
        assertEquals(userId.toString(), record.key());
        assertNotNull(record.headers().lastHeader("correlationId"));
    }

    @Test
    void publish_sameUser_samePartitionKey() {
        UUID sameUserId = UUID.randomUUID();

        TransactionEvent event1 = TransactionEvent.builder()
                .transactionId(UUID.randomUUID())
                .amount(new BigDecimal("1000"))
                .type(TransactionType.CREDIT)
                .status(TransactionStatus.PENDING)
                .build();
        event1.setUserId(sameUserId);

        TransactionEvent event2 = TransactionEvent.builder()
                .transactionId(UUID.randomUUID())
                .amount(new BigDecimal("2000"))
                .type(TransactionType.DEBIT)
                .status(TransactionStatus.PENDING)
                .build();
        event2.setUserId(sameUserId);

        when(kafkaTemplate.send(any(ProducerRecord.class)))
                .thenReturn(new CompletableFuture<>());

        producer.publishTransactionEvent(event1);
        producer.publishTransactionEvent(event2);

        ArgumentCaptor<ProducerRecord<String, Object>> captor =
                ArgumentCaptor.forClass(ProducerRecord.class);
        verify(kafkaTemplate, org.mockito.Mockito.times(2)).send(captor.capture());

        // Both events should use the same key (userId) → same partition
        String key1 = captor.getAllValues().get(0).key();
        String key2 = captor.getAllValues().get(1).key();
        assertEquals(key1, key2);
        assertEquals(sameUserId.toString(), key1);
    }
}
