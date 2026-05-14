package com.wealthsense.transaction.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wealthsense.common.constants.KafkaTopics;
import com.wealthsense.common.enums.TransactionStatus;
import com.wealthsense.common.enums.TransactionType;
import com.wealthsense.common.events.TransactionEvent;
import com.wealthsense.common.exception.DuplicateResourceException;
import com.wealthsense.common.exception.InsufficientFundsException;
import com.wealthsense.common.exception.ResourceNotFoundException;
import com.wealthsense.security.aop.Auditable;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Core transaction service — handles creation with outbox pattern.
 * Transaction + Outbox write happen in ONE @Transactional boundary.
 * Never calls Kafka directly — always via outbox table.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final OutboxRepository outboxRepository;
    private final TransactionMapper transactionMapper;
    private final ObjectMapper objectMapper;
    private final AESEncryptionService aesEncryptionService;
    private final IdempotencyService idempotencyService;
    private final DataMaskingService dataMaskingService;

    @Transactional
    @Auditable(action = "CREATE_TRANSACTION", resource = "TRANSACTION")
    public TransactionResponse createTransaction(UUID userId, String idempotencyKey,
                                                  String correlationId,
                                                  CreateTransactionRequest request) {
        IdempotencyResult<TransactionResponse> idemResult = idempotencyService.checkAndStore(
                idempotencyKey,
                "CREATE_TRANSACTION",
                () -> performCreateTransaction(userId, idempotencyKey, correlationId, request));

        return idemResult.getResult();
    }

    public TransactionResponse getTransaction(UUID transactionId, UUID userId) {
        Transaction transaction = transactionRepository.findByIdAndUserId(transactionId, userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Transaction", transactionId.toString()));
        return toMaskedResponse(transaction);
    }

    private TransactionResponse performCreateTransaction(UUID userId, String idempotencyKey,
                                                         String correlationId,
                                                         CreateTransactionRequest request) {
        if (transactionRepository.existsByIdempotencyKey(idempotencyKey)) {
            throw new DuplicateResourceException("Transaction",
                    idempotencyKey);
        }

        Account account = accountRepository.findByIdAndUserId(request.getAccountId(), userId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account", request.getAccountId().toString()));

        if (isDebitType(request.getType())) {
            if (account.getAvailableBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientFundsException(
                        account.getId(), request.getAmount(), account.getAvailableBalance());
            }
            account.setAvailableBalance(
                    account.getAvailableBalance().subtract(request.getAmount()));
            accountRepository.save(account);
        }

        Transaction transaction = Transaction.builder()
                .userId(userId)
                .accountId(request.getAccountId())
                .amount(request.getAmount())
                .type(request.getType())
                .status(TransactionStatus.PENDING)
                .category(request.getCategory())
                .merchantName(request.getMerchantName())
                .merchantId(encryptIfPresent(request.getMerchantId()))
                .description(request.getDescription())
                .idempotencyKey(idempotencyKey)
                .correlationId(correlationId)
                .metadata(encryptMetadata(request.getMetadata()))
                .build();

        Transaction saved = transactionRepository.save(transaction);
        writeToOutbox(saved, correlationId);

        log.info("Transaction created: {} type: {} amount: {} for user: {}",
                saved.getId(), saved.getType(), saved.getAmount(), userId);

        return toMaskedResponse(saved);
    }

    private boolean isDebitType(TransactionType type) {
        return type == TransactionType.DEBIT || type == TransactionType.TRANSFER;
    }

    private void writeToOutbox(Transaction transaction, String correlationId) {
        TransactionEvent event = TransactionEvent.builder()
                .transactionId(transaction.getId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .type(transaction.getType())
                .category(transaction.getCategory())
                .merchantName(transaction.getMerchantName())
                .merchantId(transaction.getMerchantId())
                .accountId(transaction.getAccountId())
                .status(transaction.getStatus())
                .build();
        event.setUserId(transaction.getUserId());
        event.setCorrelationId(correlationId);

        try {
            String payload = objectMapper.writeValueAsString(event);
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .aggregateId(transaction.getId())
                    .eventType("TRANSACTION_CREATED")
                    .payload(payload)
                    .topic(KafkaTopics.TRANSACTION_EVENTS)
                    .build();
            outboxRepository.save(outboxEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize transaction event for outbox", e);
            throw new RuntimeException("Failed to write outbox event", e);
        }
    }

    private Map<String, String> encryptMetadata(Map<String, String> metadata) {
        Map<String, String> source = metadata != null ? metadata : new HashMap<>();
        Map<String, String> encrypted = new HashMap<>();
        source.forEach((k, v) -> encrypted.put(k, encryptIfPresent(v)));
        return encrypted;
    }

    private String encryptIfPresent(String value) {
        if (!StringUtils.hasText(value)) {
            return value;
        }
        return aesEncryptionService.isEncrypted(value) ? value : aesEncryptionService.encrypt(value);
    }

    private TransactionResponse toMaskedResponse(Transaction transaction) {
        TransactionResponse response = transactionMapper.toResponse(transaction);
        String encryptedMerchantId = transaction.getMerchantId();
        if (StringUtils.hasText(encryptedMerchantId)) {
            String plainMerchantId = aesEncryptionService.isEncrypted(encryptedMerchantId)
                    ? aesEncryptionService.decrypt(encryptedMerchantId)
                    : encryptedMerchantId;
            response.setMerchantId(dataMaskingService.maskAccountNumber(plainMerchantId));
        }
        return response;
    }
}
