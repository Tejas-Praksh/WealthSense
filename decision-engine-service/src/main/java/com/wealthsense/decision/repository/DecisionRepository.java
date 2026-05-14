package com.wealthsense.decision.repository;

import com.wealthsense.decision.domain.Decision;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DecisionRepository extends MongoRepository<Decision, String> {
    
    Optional<Decision> findByTransactionId(UUID transactionId);
    
    Optional<Decision> findByDecisionId(UUID decisionId);
}
