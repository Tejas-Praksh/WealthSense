package com.wealthsense.decision.saga;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SagaOrchestratorTest {

    private SagaOrchestrator orchestrator;

    @Mock
    private SagaStep step1;

    @Mock
    private SagaStep step2;

    @Mock
    private SagaStep step3;

    private SagaContext context;

    @BeforeEach
    void setUp() {
        orchestrator = new SagaOrchestrator();
        context = SagaContext.builder()
                .transactionId(UUID.randomUUID())
                .correlationId("test-corr")
                .build();
    }

    @Test
    void execute_allStepsPass_completesSuccessfully() {
        when(step1.execute(context)).thenReturn(SagaStepResult.success());
        when(step2.execute(context)).thenReturn(SagaStepResult.success());
        when(step3.execute(context)).thenReturn(SagaStepResult.success());

        boolean result = orchestrator.executeTransaction(context, Arrays.asList(step1, step2, step3));

        assertTrue(result);
        assertFalse(context.isCompensationTriggered());

        InOrder inOrder = inOrder(step1, step2, step3);
        inOrder.verify(step1).execute(context);
        inOrder.verify(step2).execute(context);
        inOrder.verify(step3).execute(context);
    }

    @Test
    void execute_stepFails_runsCompensation() {
        when(step1.execute(context)).thenReturn(SagaStepResult.success());
        when(step2.execute(context)).thenReturn(SagaStepResult.failure("Network Error"));

        boolean result = orchestrator.executeTransaction(context, Arrays.asList(step1, step2, step3));

        assertFalse(result);
        assertTrue(context.isCompensationTriggered());

        verify(step1).execute(context);
        verify(step2).execute(context);
        verify(step3, never()).execute(context); // step 3 never executed

        verify(step1).compensate(context); // Step 1 is compensated
        verify(step2, never()).compensate(context); // Step 2 failed, not compensated
        verify(step3, never()).compensate(context);
    }

    @Test
    void execute_compensationRuns_inReverseOrder() {
        when(step1.execute(context)).thenReturn(SagaStepResult.success());
        when(step2.execute(context)).thenReturn(SagaStepResult.success());
        when(step3.execute(context)).thenReturn(SagaStepResult.failure("Out of Balance"));

        boolean result = orchestrator.executeTransaction(context, Arrays.asList(step1, step2, step3));

        assertFalse(result);
        assertTrue(context.isCompensationTriggered());

        InOrder inOrder = inOrder(step2, step1);
        inOrder.verify(step2).compensate(context);
        inOrder.verify(step1).compensate(context);
    }
}
