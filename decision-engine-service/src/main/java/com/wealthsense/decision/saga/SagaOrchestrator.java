package com.wealthsense.decision.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class SagaOrchestrator {

    public boolean executeTransaction(SagaContext context, List<SagaStep> steps) {
        List<SagaStep> completedSteps = new ArrayList<>();
        
        log.info("[{}] Starting SAGA Orchestrator with {} steps for txn {}", 
                context.getCorrelationId(), steps.size(), context.getTransactionId());
        
        for (SagaStep step : steps) {
            try {
                SagaStepResult result = step.execute(context);
                if (result.isSuccess()) {
                    completedSteps.add(step);
                    log.info("[{}] Step {} completed successfully", 
                            context.getCorrelationId(), step.getStepName());
                } else {
                    log.error("[{}] Step {} failed: {}", 
                            context.getCorrelationId(), step.getStepName(), result.getErrorMessage());
                    runCompensation(context, completedSteps);
                    return false;
                }
            } catch (Exception e) {
                log.error("[{}] Unhandled exception in step {}: {}", 
                        context.getCorrelationId(), step.getStepName(), e.getMessage());
                runCompensation(context, completedSteps);
                return false;
            }
        }
        
        log.info("[{}] SAGA Orchestrator completed successfully for txn {}", 
                context.getCorrelationId(), context.getTransactionId());
        return true;
    }

    private void runCompensation(SagaContext context, List<SagaStep> completedSteps) {
        log.warn("[{}] Triggering compensation for {} completed steps", 
                context.getCorrelationId(), completedSteps.size());
        
        context.setCompensationTriggered(true);
        
        // Compensate in reverse order
        List<SagaStep> reversed = new ArrayList<>(completedSteps);
        Collections.reverse(reversed);
        
        for (SagaStep step : reversed) {
            try {
                step.compensate(context);
                log.info("[{}] Conpensation for step {} succeeded", 
                        context.getCorrelationId(), step.getStepName());
            } catch (Exception e) {
                log.error("[{}] Compensation for step {} FAILED: {}", 
                        context.getCorrelationId(), step.getStepName(), e.getMessage());
                // In production, failure during compensation requires manual intervention or dead letter queues
            }
        }
    }
}
