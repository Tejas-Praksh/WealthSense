package com.wealthsense.transaction.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.wealthsense.common.enums.TransactionStatus;
import com.wealthsense.common.enums.TransactionType;
import com.wealthsense.common.exception.DuplicateResourceException;
import com.wealthsense.common.exception.InsufficientFundsException;
import com.wealthsense.security.encryption.AESEncryptionService;
import com.wealthsense.security.idempotency.IdempotencyResult;
import com.wealthsense.security.idempotency.IdempotencyService;
import com.wealthsense.security.masking.DataMaskingService;
import com.wealthsense.transaction.domain.Account;
import com.wealthsense.transaction.domain.OutboxEvent;
import com.wealthsense.transaction.domain.Transaction;
import com.wealthsense.transaction.dto.CreateTransactionRequest;
import com.wealthsense.transaction.dto.TransactionResponse;
import com.wealthsense.transaction.mapper.TransactionMapper;
import com.wealthsense.transaction.repository.AccountRepository;
import com.wealthsense.transaction.repository.OutboxRepository;
import com.wealthsense.transaction.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private TransactionRepository transactionRepository;
    @Mock private AccountRepository accountRepository;
    @Mock private OutboxRepository outboxRepository;
    @Mock private TransactionMapper transactionMapper;
    @Mock private AESEncryptionService aesEncryptionService;
    @Mock private IdempotencyService idempotencyService;
    @Mock private DataMaskingService dataMaskingService;
    @Spy  private ObjectMapper objectMapper = new ObjectMapper();

    @InjectMocks
    private TransactionService transactionService;

    private UUID userId;
    private UUID accountId;
    private Account account;

    @BeforeEach
    void setUp() {
        objectMapper.registerModule(new JavaTimeModule());
        userId = UUID.randomUUID();
        accountId = UUID.randomUUID();
        account = Account.builder()
                .id(accountId)
                .userId(userId)
                .balance(new BigDecimal("100000")) // 1000 rupees in paise
                .availableBalance(new BigDecimal("100000"))
                .build();

        when(idempotencyService.checkAndStore(anyString(), anyString(), any()))
                .thenAnswer(invocation -> {
                    @SuppressWarnings("unchecked")
                    Supplier<TransactionResponse> supplier = invocation.getArgument(2);
                    return IdempotencyResult.fresh(supplier.get(), Instant.now());
                });
    }

    @Test
    void createTransaction_validDebit_savesAndPublishes() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("5000")) // 50 rupees
                .type(TransactionType.DEBIT)
                .merchantName("TestMerchant")
                .build();

        when(transactionRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));

        Transaction savedTxn = Transaction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .accountId(accountId)
                .amount(request.getAmount())
                .type(TransactionType.DEBIT)
                .status(TransactionStatus.PENDING)
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTxn);
        when(outboxRepository.save(any(OutboxEvent.class))).thenReturn(new OutboxEvent());

        TransactionResponse expectedResp = TransactionResponse.builder()
                .id(savedTxn.getId()).build();
        when(transactionMapper.toResponse(any())).thenReturn(expectedResp);

        TransactionResponse result = transactionService.createTransaction(
                userId, "idem-key-1", "corr-123", request);

        assertNotNull(result);
        verify(transactionRepository).save(any(Transaction.class));
        verify(outboxRepository).save(any(OutboxEvent.class));
        verify(accountRepository).save(any(Account.class));

        // Balance should be deducted
        assertEquals(new BigDecimal("95000"), account.getAvailableBalance());
    }

    @Test
    void createTransaction_insufficientFunds_throwsException() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("200000")) // More than balance
                .type(TransactionType.DEBIT)
                .build();

        when(transactionRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));

        assertThrows(InsufficientFundsException.class, () ->
                transactionService.createTransaction(userId, "idem-2", "corr-456", request));

        verify(transactionRepository, never()).save(any());
        verify(outboxRepository, never()).save(any());
    }

    @Test
    void createTransaction_duplicateIdempotencyKey_throwsException() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("5000"))
                .type(TransactionType.DEBIT)
                .build();

        when(transactionRepository.existsByIdempotencyKey("duplicate-key")).thenReturn(true);

        assertThrows(DuplicateResourceException.class, () ->
                transactionService.createTransaction(userId, "duplicate-key", "corr-789", request));

        verify(accountRepository, never()).findByIdAndUserId(any(), any());
    }

    @Test
    void createTransaction_savesOutboxEvent_atomically() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("3000"))
                .type(TransactionType.CREDIT) // No balance check for credit
                .category("SALARY")
                .build();

        when(transactionRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));

        Transaction savedTxn = Transaction.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .accountId(accountId)
                .amount(request.getAmount())
                .type(TransactionType.CREDIT)
                .status(TransactionStatus.PENDING)
                .currency("INR")
                .category("SALARY")
                .build();
        when(transactionRepository.save(any(Transaction.class))).thenReturn(savedTxn);
        when(outboxRepository.save(any(OutboxEvent.class))).thenReturn(new OutboxEvent());
        when(transactionMapper.toResponse(any())).thenReturn(new TransactionResponse());

        transactionService.createTransaction(userId, "idem-3", "corr-abc", request);

        ArgumentCaptor<OutboxEvent> outboxCaptor = ArgumentCaptor.forClass(OutboxEvent.class);
        verify(outboxRepository).save(outboxCaptor.capture());

        OutboxEvent capturedEvent = outboxCaptor.getValue();
        assertEquals("PENDING", capturedEvent.getStatus());
        assertEquals("TRANSACTION_CREATED", capturedEvent.getEventType());
        assertEquals("transaction-events", capturedEvent.getTopic());
        assertNotNull(capturedEvent.getPayload());
    }

    @Test
    void createTransaction_transactionSaveFails_outboxNotSaved() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("1000"))
                .type(TransactionType.DEBIT)
                .build();

        when(transactionRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class)))
                .thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () ->
                transactionService.createTransaction(userId, "idem-fail", "corr", request));

        verify(outboxRepository, never()).save(any());
    }

    @Test
    void createTransaction_creditType_noBalanceCheck() {
        CreateTransactionRequest request = CreateTransactionRequest.builder()
                .accountId(accountId)
                .amount(new BigDecimal("500000")) // Large credit
                .type(TransactionType.CREDIT)
                .build();

        when(transactionRepository.existsByIdempotencyKey(anyString())).thenReturn(false);
        when(accountRepository.findByIdAndUserId(accountId, userId)).thenReturn(Optional.of(account));

        Transaction savedTxn = Transaction.builder()
                .id(UUID.randomUUID()).userId(userId).accountId(accountId)
                .amount(request.getAmount()).type(TransactionType.CREDIT).build();
        when(transactionRepository.save(any())).thenReturn(savedTxn);
        when(outboxRepository.save(any())).thenReturn(new OutboxEvent());
        when(transactionMapper.toResponse(any())).thenReturn(new TransactionResponse());

        // Should NOT throw InsufficientFundsException for CREDIT
        assertDoesNotThrow(() ->
                transactionService.createTransaction(userId, "idem-4", "corr-def", request));

        // Balance should NOT be deducted for credit
        assertEquals(new BigDecimal("100000"), account.getAvailableBalance());
    }
}
