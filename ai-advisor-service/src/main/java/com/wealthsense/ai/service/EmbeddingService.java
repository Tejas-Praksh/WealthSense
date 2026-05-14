package com.wealthsense.ai.service;

import com.wealthsense.ai.domain.TransactionEmbedding;
import com.wealthsense.ai.repository.TransactionEmbeddingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final TransactionEmbeddingRepository repository;
    private final EmbeddingModel embeddingModel;

    public void generateEmbedding(UUID userId, UUID transactionId, String text) {
        try {
            float[] rawVector = embeddingModel.embed(text);
            List<Double> embeddingVector = IntStream.range(0, rawVector.length)
                    .mapToDouble(i -> rawVector[i])
                    .boxed()
                    .toList();
            TransactionEmbedding te = TransactionEmbedding.builder()
                .userId(userId)
                .transactionId(transactionId)
                .content(text)
                .embedding(embeddingVector)
                .build();
            repository.save(te);
            log.info("Saved embedding for transaction {}", transactionId);
        } catch (Exception e) {
            log.error("Failed to generate embedding", e);
        }
    }
}
