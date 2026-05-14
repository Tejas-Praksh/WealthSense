package com.wealthsense.ai.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthsense.ai.service.EmbeddingService;
import com.wealthsense.common.constants.KafkaTopics;
import com.wealthsense.common.events.TransactionEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransactionEventConsumer {

    private final EmbeddingService embeddingService;
    private final ObjectMapper objectMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    @KafkaListener(topics = KafkaTopics.TRANSACTION_EVENTS, groupId = "ai-advisor-service-group")
    public void consumeTransactionEvent(String message) {
        try {
            TransactionEvent event = objectMapper.readValue(message, TransactionEvent.class);
            log.info("Received transaction event: {}", event.getTransactionId());

            String textRepresentation = String.format("Transaction of %s %s at %s for category %s.",
                    event.getAmount(), event.getCurrency(), event.getMerchantName(), event.getCategory());

            // 1. Generate text rep and store embedding
            embeddingService.generateEmbedding(event.getUserId(), event.getTransactionId(), textRepresentation);

            // 2. Invalidate user's insight cache 
            // Depending on how insights are cached, clear the key
            redisTemplate.delete("ai:insights:" + event.getUserId());
            
        } catch (Exception e) {
            log.error("Error processing transaction event", e);
        }
    }
}
