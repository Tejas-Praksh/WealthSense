package com.wealthsense.decision.saga;

public interface SagaStep {
    String getStepName();
    SagaStepResult execute(SagaContext context);
    void compensate(SagaContext context);
}
