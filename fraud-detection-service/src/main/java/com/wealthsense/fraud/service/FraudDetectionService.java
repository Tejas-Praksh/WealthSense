package com.wealthsense.fraud.service;

import com.wealthsense.common.events.FraudAlertEvent;
import com.wealthsense.common.events.TransactionEvent;
import com.wealthsense.fraud.domain.FraudAlert;
import com.wealthsense.fraud.kafka.FraudAlertProducer;
import com.wealthsense.fraud.repository.FraudAlertRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Orchestrates fraud detection:
 * 1. Load recent transactions from Redis
 * 2. Run risk scoring (parallel rules)
 * 3. Save FraudAlert to PostgreSQL
 * 4. Publish FraudAlertEvent to Kafka
 * 5. Update Redis history
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FraudDetectionService {

    private static final String REDIS_TXN_PREFIX = "fraud:txn:";
    private static final int MAX_RECENT = 20;
    private static final Duration TXN_HISTORY_TTL = Duration.ofHours(2);

    private final RiskScoringService riskScoringService;
    private final FraudAlertRepository fraudAlertRepository;
    private final FraudAlertProducer fraudAlertProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    public void processTransaction(TransactionEvent event) {
        String correlationId = event.getCorrelationId();
        log.info("[{}] Processing fraud check for txn: {}",
                correlationId, event.getTransactionId());

        // 1. Load recent transactions from Redis (graceful degradation)
        List<TransactionEvent> recentTransactions = loadRecentTransactions(
                event.getUserId().toString());

        // 2. Run risk scoring (all rules in parallel)
        RiskScoringService.RiskResult result =
                riskScoringService.calculateRisk(event, recentTransactions);

        // 3. Save fraud alert to PostgreSQL
        FraudAlert alert = FraudAlert.builder()
                .transactionId(event.getTransactionId())
                .userId(event.getUserId())
                .severity(result.severity())
                .riskScore(result.riskScore())
                .ruleTriggered(result.triggeredRules())
                .recommendedAction(result.recommendedAction())
                .reason("Risk score: " + String.format("%.4f", result.riskScore())
                        + " - " + result.triggeredRules())
                .evidence(result.evidence())
                .build();
        fraudAlertRepository.save(alert);

        // 4. Publish FraudAlertEvent to Kafka
        FraudAlertEvent alertEvent = FraudAlertEvent.builder()
                .transactionId(event.getTransactionId())
                .alertId(alert.getId())
                .severity(result.severity())
                .riskScore(result.riskScore())
                .ruleTriggered(result.triggeredRules())
                .recommendedAction(result.recommendedAction())
                .reason(alert.getReason())
                .evidence(result.evidence())
                .build();
        alertEvent.setUserId(event.getUserId());
        alertEvent.setCorrelationId(correlationId);
        alertEvent.setEventType("FRAUD_ALERT");
        alertEvent.setSource("fraud-detection-service");

        fraudAlertProducer.publishFraudAlert(alertEvent);

        // 5. Update Redis transaction history
        updateTransactionHistory(event);

        log.info("[{}] Fraud check completed for txn: {} → {} (score: {})",
                correlationId, event.getTransactionId(),
                result.recommendedAction(),
                String.format("%.4f", result.riskScore()));
    }

    @SuppressWarnings("unchecked")
    List<TransactionEvent> loadRecentTransactions(String userId) {
        try {
            String key = REDIS_TXN_PREFIX + userId;
            List<Object> raw = redisTemplate.opsForList().range(key, 0, MAX_RECENT - 1);
            if (raw == null || raw.isEmpty()) return Collections.emptyList();
            return raw.stream()
                    .filter(TransactionEvent.class::isInstance)
                    .map(TransactionEvent.class::cast)
                    .toList();
        } catch (Exception e) {
            log.warn("Redis unavailable for user history: {}", e.getMessage());
            return Collections.emptyList(); // Graceful degradation
        }
    }

    void updateTransactionHistory(TransactionEvent event) {
        try {
            String key = REDIS_TXN_PREFIX + event.getUserId();
            redisTemplate.opsForList().leftPush(key, event);
            redisTemplate.opsForList().trim(key, 0, MAX_RECENT - 1);
            redisTemplate.expire(key, TXN_HISTORY_TTL);
        } catch (Exception e) {
            log.warn("Redis update failed for txn history: {}", e.getMessage());
            // Graceful degradation — fraud check still completes
        }
    }
}
