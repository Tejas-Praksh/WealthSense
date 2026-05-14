package com.wealthsense.decision.service;

import com.wealthsense.common.enums.DecisionType;
import com.wealthsense.common.enums.FraudSeverity;
import com.wealthsense.common.events.FraudAlertEvent;
import com.wealthsense.decision.domain.Decision;
import com.wealthsense.decision.kafka.DecisionEventProducer;
import com.wealthsense.decision.repository.DecisionRepository;
import com.wealthsense.decision.saga.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

    @Mock private DecisionRepository decisionRepository;
    @Mock private SagaOrchestrator sagaOrchestrator;
    @Mock private ValidateBalanceStep validateBalanceStep;
    @Mock private ApproveTransactionStep approveTransactionStep;
    @Mock private RejectTransactionStep rejectTransactionStep;
    @Mock private DecisionEventProducer decisionEventProducer;

    @InjectMocks
    private DecisionService decisionService;

    private FraudAlertEvent event;

    @BeforeEach
    void setUp() {
        Map<String, Object> evidence = new HashMap<>();
        evidence.put("amount", "100.00");

        event = FraudAlertEvent.builder()
                .transactionId(UUID.randomUUID())
                .alertId(UUID.randomUUID())
                .ruleTriggered("VelocityRule")
                .riskScore(0.2)
                .evidence(evidence)
                .build();
        event.setUserId(UUID.randomUUID());
        event.setCorrelationId("test-corr");
    }

    @Test
    void processFraudAlert_lowSeverity_runsApprove() {
        event.setSeverity(FraudSeverity.LOW);
        when(sagaOrchestrator.executeTransaction(any(SagaContext.class), anyList())).thenReturn(true);

        decisionService.processFraudAlert(event);

        verify(sagaOrchestrator, times(1)).executeTransaction(any(), any());
        
        ArgumentCaptor<Decision> decisionCaptor = ArgumentCaptor.forClass(Decision.class);
        verify(decisionRepository).save(decisionCaptor.capture());
        
        Decision savedDecision = decisionCaptor.getValue();
        assertEquals(DecisionType.APPROVE, savedDecision.getDecision());
        assertEquals("COMPLETED", savedDecision.getSagaStatus());
        assertEquals("test-corr", savedDecision.getCorrelationId());
    }

    @Test
    void processFraudAlert_criticalSeverity_runsReject() {
        event.setSeverity(FraudSeverity.CRITICAL);
        event.setRiskScore(0.9);
        when(sagaOrchestrator.executeTransaction(any(SagaContext.class), anyList())).thenReturn(true);

        decisionService.processFraudAlert(event);

        ArgumentCaptor<Decision> decisionCaptor = ArgumentCaptor.forClass(Decision.class);
        verify(decisionRepository).save(decisionCaptor.capture());
        
        Decision savedDecision = decisionCaptor.getValue();
        assertEquals(DecisionType.REJECT, savedDecision.getDecision());
        assertEquals(0.1, savedDecision.getConfidence(), 0.001); // 1.0 - 0.9 = 0.1
    }

    @Test
    void processFraudAlert_mediumSeverity_holdsForReview() {
        event.setSeverity(FraudSeverity.MEDIUM);

        decisionService.processFraudAlert(event);

        verify(sagaOrchestrator, never()).executeTransaction(any(), any());
        verify(decisionEventProducer, times(1)).publishNotification(any());
        
        ArgumentCaptor<Decision> decisionCaptor = ArgumentCaptor.forClass(Decision.class);
        verify(decisionRepository).save(decisionCaptor.capture());
        
        Decision savedDecision = decisionCaptor.getValue();
        assertEquals(DecisionType.HOLD, savedDecision.getDecision());
        assertEquals("PENDING_REVIEW", savedDecision.getSagaStatus());
    }

    @Test
    void processFraudAlert_sagaFails_updatesSagaStatusToFailed() {
        event.setSeverity(FraudSeverity.LOW);
        when(sagaOrchestrator.executeTransaction(any(SagaContext.class), anyList())).thenReturn(false);

        decisionService.processFraudAlert(event);

        ArgumentCaptor<Decision> decisionCaptor = ArgumentCaptor.forClass(Decision.class);
        verify(decisionRepository).save(decisionCaptor.capture());
        
        Decision savedDecision = decisionCaptor.getValue();
        assertEquals("FAILED_COMPENSATED", savedDecision.getSagaStatus());
    }
}
