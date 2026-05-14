package com.wealthsense.decision.saga;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SagaStepResult {
    private boolean success;
    private String errorMessage;
    private Object payload;
    
    public static SagaStepResult success() {
        return new SagaStepResult(true, null, null);
    }
    
    public static SagaStepResult success(Object payload) {
        return new SagaStepResult(true, null, payload);
    }
    
    public static SagaStepResult failure(String errorMessage) {
        return new SagaStepResult(false, errorMessage, null);
    }
}
