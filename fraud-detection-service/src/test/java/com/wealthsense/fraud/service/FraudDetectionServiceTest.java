package com.wealthsense.fraud.service;

import com.wealthsense.common.enums.FraudSeverity;
import com.wealthsense.common.enums.TransactionStatus;
import com.wealthsense.common.enums.TransactionType;
import com.wealthsense.common.events.FraudAlertEvent;
import com.wealthsense.common.events.TransactionEvent;
import com.wealthsense.fraud.domain.FraudAlert;
import com.wealthsense.fraud.kafka.FraudAlertProducer;
import com.wealthsense.fraud.repository.FraudAlertRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FraudDetectionServiceTest {

    @Mock private RiskScoringService riskScoringService;
    @Mock private FraudAlertRepository fraudAlertRepository;
    @Mock private FraudAlertProducer fraudAlertProducer;
    @Mock private RedisTemplate<String, Object> redisTemplate;
    @Mock private ListOperations<String, Object> listOperations;

    private FraudDetectionService fraudDetectionService;

    @BeforeEach
    void setUp() {
        fraudDetectionService = new FraudDetectionService(
                riskScoringService,
                fraudAlertRepository,
                fraudAlertProducer,
                redisTemplate
        );
    }

    @Test
    void processTransaction_lowRisk_publishesApprove() {
        TransactionEvent event = buildEvent();
        RiskScoringService.RiskResult lowResult = new RiskScoringService.RiskResult(
                0.1, FraudSeverity.LOW, "APPROVE",
                "", Collections.emptyMap());

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(any(), anyLong(), anyLong())).thenReturn(null);
        when(riskScoringService.calculateRisk(any(), any())).thenReturn(lowResult);
        when(fraudAlertRepository.save(any(FraudAlert.class)))
                .thenAnswer(inv -> {
                    FraudAlert alert = inv.getArgument(0);
                    alert.setId(UUID.randomUUID());
                    return alert;
                });

        fraudDetectionService.processTransaction(event);

        // Verify Kafka publish with APPROVE action
        ArgumentCaptor<FraudAlertEvent> captor =
                ArgumentCaptor.forClass(FraudAlertEvent.class);
        verify(fraudAlertProducer).publishFraudAlert(captor.capture());
        assertEquals("APPROVE", captor.getValue().getRecommendedAction());
        assertEquals(FraudSeverity.LOW, captor.getValue().getSeverity());
    }

    @Test
    void processTransaction_highRisk_publishesBlock() {
        TransactionEvent event = buildEvent();
        RiskScoringService.RiskResult highResult = new RiskScoringService.RiskResult(
                0.9, FraudSeverity.CRITICAL, "BLOCK",
                "LARGE_AMOUNT=0.80", Collections.emptyMap());

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(any(), anyLong(), anyLong())).thenReturn(null);
        when(riskScoringService.calculateRisk(any(), any())).thenReturn(highResult);
        when(fraudAlertRepository.save(any(FraudAlert.class)))
                .thenAnswer(inv -> {
                    FraudAlert alert = inv.getArgument(0);
                    alert.setId(UUID.randomUUID());
                    return alert;
                });

        fraudDetectionService.processTransaction(event);

        ArgumentCaptor<FraudAlertEvent> captor =
                ArgumentCaptor.forClass(FraudAlertEvent.class);
        verify(fraudAlertProducer).publishFraudAlert(captor.capture());
        assertEquals("BLOCK", captor.getValue().getRecommendedAction());
        assertEquals(FraudSeverity.CRITICAL, captor.getValue().getSeverity());
    }

    @Test
    void processTransaction_savesAlertToDatabase() {
        TransactionEvent event = buildEvent();
        RiskScoringService.RiskResult result = new RiskScoringService.RiskResult(
                0.5, FraudSeverity.MEDIUM, "REVIEW",
                "VELOCITY=0.40", Collections.emptyMap());

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(any(), anyLong(), anyLong())).thenReturn(null);
        when(riskScoringService.calculateRisk(any(), any())).thenReturn(result);
        when(fraudAlertRepository.save(any(FraudAlert.class)))
                .thenAnswer(inv -> {
                    FraudAlert alert = inv.getArgument(0);
                    alert.setId(UUID.randomUUID());
                    return alert;
                });

        fraudDetectionService.processTransaction(event);

        ArgumentCaptor<FraudAlert> dbCaptor =
                ArgumentCaptor.forClass(FraudAlert.class);
        verify(fraudAlertRepository).save(dbCaptor.capture());

        FraudAlert saved = dbCaptor.getValue();
        assertEquals(event.getTransactionId(), saved.getTransactionId());
        assertEquals(event.getUserId(), saved.getUserId());
        assertEquals(FraudSeverity.MEDIUM, saved.getSeverity());
        assertEquals(0.5, saved.getRiskScore(), 0.001);
        assertEquals("REVIEW", saved.getRecommendedAction());
    }

    @Test
    void processTransaction_updatesRedisHistory() {
        TransactionEvent event = buildEvent();
        RiskScoringService.RiskResult result = new RiskScoringService.RiskResult(
                0.1, FraudSeverity.LOW, "APPROVE",
                "", Collections.emptyMap());

        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(any(), anyLong(), anyLong())).thenReturn(null);
        when(riskScoringService.calculateRisk(any(), any())).thenReturn(result);
        when(fraudAlertRepository.save(any(FraudAlert.class)))
                .thenAnswer(inv -> {
                    FraudAlert alert = inv.getArgument(0);
                    alert.setId(UUID.randomUUID());
                    return alert;
                });

        fraudDetectionService.processTransaction(event);

        // Verify Redis history updated
        String key = "fraud:txn:" + event.getUserId();
        verify(listOperations).leftPush(key, event);
        verify(listOperations).trim(key, 0, 19);
        verify(redisTemplate).expire(eq(key), any());
    }

    @Test
    void processTransaction_redisUnavailable_stillCompletes() {
        TransactionEvent event = buildEvent();
        RiskScoringService.RiskResult result = new RiskScoringService.RiskResult(
                0.1, FraudSeverity.LOW, "APPROVE",
                "", Collections.emptyMap());

        // Redis throws on read → graceful degradation
        when(redisTemplate.opsForList()).thenReturn(listOperations);
        when(listOperations.range(any(), anyLong(), anyLong()))
                .thenThrow(new RuntimeException("Redis down"));
        when(riskScoringService.calculateRisk(any(), any())).thenReturn(result);
        when(fraudAlertRepository.save(any(FraudAlert.class)))
                .thenAnswer(inv -> {
                    FraudAlert alert = inv.getArgument(0);
                    alert.setId(UUID.randomUUID());
                    return alert;
                });

        // Should NOT throw — graceful degradation
        assertDoesNotThrow(() ->
                fraudDetectionService.processTransaction(event));

        // Still publishes alert and saves to DB
        verify(fraudAlertProducer).publishFraudAlert(any());
        verify(fraudAlertRepository).save(any());
    }

    // ========== Helpers ==========

    private TransactionEvent buildEvent() {
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(UUID.randomUUID())
                .amount(new BigDecimal("5000"))
                .type(TransactionType.DEBIT)
                .status(TransactionStatus.PENDING)
                .accountId(UUID.randomUUID())
                .merchantName("TestMerchant")
                .build();
        event.setUserId(UUID.randomUUID());
        event.setCorrelationId("corr-" + UUID.randomUUID());
        event.setTimestamp(Instant.now());
        return event;
    }
}
