package com.wealthsense.decision.saga;

import com.wealthsense.common.enums.DecisionType;
import com.wealthsense.common.events.DecisionEvent;
import com.wealthsense.decision.kafka.DecisionEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ApproveTransactionStepTest {

    @Mock
    private DecisionEventProducer decisionEventProducer;

    @InjectMocks
    private ApproveTransactionStep step;

    private SagaContext context;

    @BeforeEach
    void setUp() {
        context = SagaContext.builder()
                .transactionId(UUID.randomUUID())
                .userId(UUID.randomUUID())
                .correlationId("corr-789")
                .build();
    }

    @Test
    void execute_validTransaction_updatesBalance() {
        SagaStepResult result = step.execute(context);

        assertTrue(result.isSuccess());
        assertEquals("ApprovedAndPublished", context.getResult("ApproveTransaction"));

        ArgumentCaptor<DecisionEvent> captor = ArgumentCaptor.forClass(DecisionEvent.class);
        verify(decisionEventProducer, times(1)).publishDecision(captor.capture());

        DecisionEvent event = captor.getValue();
        assertEquals(context.getTransactionId(), event.getTransactionId());
        assertEquals(context.getUserId(), event.getUserId());
        assertEquals(DecisionType.APPROVE, event.getDecision());
        assertEquals("corr-789", event.getCorrelationId());
    }

    @Test
    void compensate_reversesBalanceDeduction() {
        step.compensate(context);

        ArgumentCaptor<DecisionEvent> captor = ArgumentCaptor.forClass(DecisionEvent.class);
        verify(decisionEventProducer, times(1)).publishDecision(captor.capture());

        DecisionEvent event = captor.getValue();
        assertEquals(context.getTransactionId(), event.getTransactionId());
        assertEquals(context.getUserId(), event.getUserId());
        assertEquals(DecisionType.REJECT, event.getDecision());
    }
}
