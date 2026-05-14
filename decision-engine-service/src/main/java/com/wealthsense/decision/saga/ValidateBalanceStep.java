package com.wealthsense.decision.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateBalanceStep implements SagaStep {

    @Override
    public String getStepName() {
        return "ValidateBalance";
    }

    @Override
    public SagaStepResult execute(SagaContext context) {
        log.info("[{}] Executing ValidateBalanceStep for txn: {}", 
                context.getCorrelationId(), context.getTransactionId());
        
        // In a real distributed system, this would make an synchronous REST call 
        // to transaction-service/account-service to hold/validate balance.
        // For this SAGA demonstration, we assume valid balance unless amount is negative.
        if (context.getAmount() != null && context.getAmount().signum() < 0) {
            return SagaStepResult.failure("Invalid amount detected");
        }
        
        context.addResult(getStepName(), "BalanceValidated");
        return SagaStepResult.success("Balance validated");
    }

    @Override
    public void compensate(SagaContext context) {
        log.info("[{}] Compensating ValidateBalanceStep for txn: {} - No action needed (Read-only)", 
                context.getCorrelationId(), context.getTransactionId());
    }
}
