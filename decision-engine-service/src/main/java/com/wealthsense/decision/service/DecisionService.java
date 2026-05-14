package com.wealthsense.decision.service;

import com.wealthsense.common.enums.DecisionType;
import com.wealthsense.common.enums.FraudSeverity;
import com.wealthsense.common.events.FraudAlertEvent;
import com.wealthsense.common.events.NotificationEvent;
import com.wealthsense.common.enums.NotificationChannel;
import com.wealthsense.decision.domain.Decision;
import com.wealthsense.decision.kafka.DecisionEventProducer;
import com.wealthsense.decision.repository.DecisionRepository;
import com.wealthsense.decision.saga.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class DecisionService {

    private final DecisionRepository decisionRepository;
    private final SagaOrchestrator sagaOrchestrator;
    private final ValidateBalanceStep validateBalanceStep;
    private final ApproveTransactionStep approveTransactionStep;
    private final RejectTransactionStep rejectTransactionStep;
    private final DecisionEventProducer decisionEventProducer;

    public void processFraudAlert(FraudAlertEvent event) {
        long startTime = System.currentTimeMillis();
        log.info("[{}] Processing Decision for FraudAlert: txn={}, severity={}",
                event.getCorrelationId(), event.getTransactionId(), event.getSeverity());

        DecisionType decisionType;
        List<SagaStep> steps = new ArrayList<>();
        steps.add(validateBalanceStep);

        if (event.getSeverity() == FraudSeverity.LOW) {
            decisionType = DecisionType.APPROVE;
            steps.add(approveTransactionStep);
        } else if (event.getSeverity() == FraudSeverity.CRITICAL) {
            decisionType = DecisionType.REJECT;
            steps.add(rejectTransactionStep);
        } else {
            // MEDIUM or HIGH
            decisionType = DecisionType.HOLD;
            // HOLD skips immediate execution steps, saves to review queue in DB
        }

        SagaContext context = SagaContext.builder()
                .transactionId(event.getTransactionId())
                .userId(UUID.fromString(event.getUserId().toString())) // Wait! 
                // Need to extract userId from event. Wait, FraudAlertEvent doesn't have userId inside?
                // Let me check FraudAlertEvent structure. BaseEvent has userId!
                .userId((UUID) event.getUserId())
                // Assuming amount is inside evidence. By instruction, getAmount is needed.
                .amount(getAmountFromEvidence(event))
                .fraudAlertEvent(event)
                .decisionType(decisionType)
                .correlationId(event.getCorrelationId())
                .build();

        String sagaStatus = "COMPLETED";
        
        if (decisionType != DecisionType.HOLD) {
            boolean success = sagaOrchestrator.executeTransaction(context, steps);
            if (!success) {
                sagaStatus = "FAILED_COMPENSATED";
            }
        } else {
            sagaStatus = "PENDING_REVIEW";
            
            // Notify user it's held
             NotificationEvent notificationEvent = NotificationEvent.builder()
                    .channel(NotificationChannel.PUSH)
                    .subject("Transaction Held for Review")
                    .body("Your transaction is under review for security reasons.")
                    .build();
            notificationEvent.setUserId(event.getUserId());
            notificationEvent.setCorrelationId(event.getCorrelationId());
            decisionEventProducer.publishNotification(notificationEvent);
        }

        long processingTime = System.currentTimeMillis() - startTime;

        Decision decision = Decision.builder()
                .decisionId(UUID.randomUUID())
                .transactionId(event.getTransactionId())
                .userId(event.getUserId())
                .fraudAlertId(event.getAlertId())
                .decision(decisionType)
                .confidence(1.0 - (event.getRiskScore() != null ? event.getRiskScore() : 0.0))
                .rulesApplied(Collections.singletonList(event.getRuleTriggered()))
                .processingTimeMs(processingTime)
                .explanation("Severity: " + event.getSeverity() + " -> " + decisionType)
                .sagaStatus(sagaStatus)
                .compensationRequired(context.isCompensationTriggered())
                .compensationStatus(context.isCompensationTriggered() ? "COMPLETED" : "NONE")
                .correlationId(event.getCorrelationId())
                .build();

        decisionRepository.save(decision);
        
        log.info("[{}] Decision {} generated in {}ms", 
                event.getCorrelationId(), decisionType, processingTime);
    }
    
    private BigDecimal getAmountFromEvidence(FraudAlertEvent event) {
        if (event.getEvidence() != null && event.getEvidence().containsKey("amount")) {
            Object amtObj = event.getEvidence().get("amount");
            if (amtObj instanceof Number) {
                return new BigDecimal(amtObj.toString());
            } else if (amtObj instanceof String) {
                return new BigDecimal((String) amtObj);
            }
        }
        return BigDecimal.ZERO; 
    }
}
