package com.wealthsense.decision.domain;

import com.wealthsense.common.enums.DecisionType;
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

@Document(collection = "decisions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Decision {

    @Id
    private String id;
    
    private UUID decisionId;
    private UUID transactionId;
    private UUID userId;
    private UUID fraudAlertId;
    
    private DecisionType decision;
    private Double confidence;
    private List<String> rulesApplied;
    
    private Long processingTimeMs;
    private String explanation;
    
    private String sagaStatus;
    private Boolean compensationRequired;
    private String compensationStatus;
    private String correlationId;
    
    @CreatedDate
    private Instant createdAt;
    
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
}
