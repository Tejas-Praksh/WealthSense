package com.wealthsense.ai.repository;

import com.wealthsense.ai.domain.TransactionEmbedding;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface TransactionEmbeddingRepository extends MongoRepository<TransactionEmbedding, String> {
    
    List<TransactionEmbedding> findByUserId(UUID userId);
}
