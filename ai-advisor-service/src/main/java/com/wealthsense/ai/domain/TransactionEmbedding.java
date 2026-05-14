package com.wealthsense.ai.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "transaction_embeddings")
public class TransactionEmbedding {

    @Id
    private String id;
    private UUID userId;
    private UUID transactionId;
    
    private String content; // text representation
    private List<Double> embedding;
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    @CreatedDate
    private Instant createdAt;
}
