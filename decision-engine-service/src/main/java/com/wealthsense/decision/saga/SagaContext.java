package com.wealthsense.decision.saga;

import com.wealthsense.common.enums.DecisionType;
import com.wealthsense.common.events.FraudAlertEvent;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
public class SagaContext {
    private UUID transactionId;
    private UUID userId;
    private BigDecimal amount;
    private FraudAlertEvent fraudAlertEvent;
    private DecisionType decisionType;
    @Builder.Default
    private Map<String, Object> stepResults = new HashMap<>();
    private String correlationId;
    @Builder.Default
    private boolean compensationTriggered = false;
    
    public void addResult(String stepName, Object result) {
        this.stepResults.put(stepName, result);
    }
    
    public Object getResult(String stepName) {
        return this.stepResults.get(stepName);
    }
}
