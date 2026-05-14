package com.wealthsense.fraud.repository;

import com.wealthsense.fraud.domain.FraudAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface FraudAlertRepository extends JpaRepository<FraudAlert, UUID> {

    List<FraudAlert> findByUserIdOrderByCreatedAtDesc(UUID userId);

    List<FraudAlert> findByTransactionId(UUID transactionId);
}
