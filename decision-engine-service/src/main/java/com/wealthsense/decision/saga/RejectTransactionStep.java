package com.wealthsense.decision.saga;

import com.wealthsense.common.enums.DecisionType;
import com.wealthsense.common.enums.NotificationChannel;
import com.wealthsense.common.events.DecisionEvent;
import com.wealthsense.common.events.NotificationEvent;
import com.wealthsense.decision.kafka.DecisionEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class RejectTransactionStep implements SagaStep {

    private final DecisionEventProducer decisionEventProducer;

    @Override
    public String getStepName() {
        return "RejectTransaction";
    }

    @Override
    public SagaStepResult execute(SagaContext context) {
        log.info("[{}] Executing RejectTransactionStep for txn: {}", 
                context.getCorrelationId(), context.getTransactionId());
                
        try {
            DecisionEvent decisionEvent = DecisionEvent.builder()
                    .transactionId(context.getTransactionId())
                    .decision(DecisionType.REJECT)
                    .confidence(1.0)
                    .explanation("Rejected by SAGA workflow")
                    .build();
            decisionEvent.setUserId(context.getUserId());
            decisionEvent.setCorrelationId(context.getCorrelationId());
            
            decisionEventProducer.publishDecision(decisionEvent);
            
            NotificationEvent notificationEvent = NotificationEvent.builder()
                    .channel(NotificationChannel.PUSH)
                    .subject("Transaction Blocked")
                    .body("Your transaction has been blocked due to suspicious activity.")
                    .build();
            notificationEvent.setUserId(context.getUserId());
            notificationEvent.setCorrelationId(context.getCorrelationId());
            
            decisionEventProducer.publishNotification(notificationEvent);
            
            context.addResult(getStepName(), "RejectedAndNotified");
            return SagaStepResult.success("Reject handling complete");
        } catch (Exception e) {
            log.error("Failed to execute reject flow", e);
            return SagaStepResult.failure(e.getMessage());
        }
    }

    @Override
    public void compensate(SagaContext context) {
        log.info("[{}] Compensating RejectTransactionStep for txn: {} - No action (already rejected)", 
                context.getCorrelationId(), context.getTransactionId());
    }
}
