package com.wealthsense.decision.saga;

import com.wealthsense.common.enums.DecisionType;
import com.wealthsense.common.events.DecisionEvent;
import com.wealthsense.decision.kafka.DecisionEventProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class ApproveTransactionStep implements SagaStep {

    private final DecisionEventProducer decisionEventProducer;

    @Override
    public String getStepName() {
        return "ApproveTransaction";
    }

    @Override
    public SagaStepResult execute(SagaContext context) {
        log.info("[{}] Executing ApproveTransactionStep for txn: {}", 
                context.getCorrelationId(), context.getTransactionId());

        try {
            // Logically updates status to COMPLETED and deducts bal (via distributed system mechanisms)
            DecisionEvent event = DecisionEvent.builder()
                    .transactionId(context.getTransactionId())
                    .decision(DecisionType.APPROVE)
                    .confidence(1.0)
                    .explanation("Approved by SAGA workflow")
                    .build();
            event.setUserId(context.getUserId());
            event.setCorrelationId(context.getCorrelationId());
            
            decisionEventProducer.publishDecision(event);
            context.addResult(getStepName(), "ApprovedAndPublished");
            return SagaStepResult.success("Approval successful");
        } catch (Exception e) {
            log.error("Failed to approve transaction", e);
            return SagaStepResult.failure(e.getMessage());
        }
    }

    @Override
    public void compensate(SagaContext context) {
        log.info("[{}] Compensating ApproveTransactionStep for txn: {}", 
                context.getCorrelationId(), context.getTransactionId());
        
        // Reverse balance deduction and update to REVERSED
        DecisionEvent event = DecisionEvent.builder()
                .transactionId(context.getTransactionId())
                .decision(DecisionType.REJECT) // Reversing previously approved
                .confidence(1.0)
                .explanation("Compensated by SAGA workflow")
                .build();
        event.setUserId(context.getUserId());
        event.setCorrelationId(context.getCorrelationId());
        
        decisionEventProducer.publishDecision(event);
    }
}
