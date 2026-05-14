package com.wealthsense.transaction.service;

import com.wealthsense.common.dto.PagedResponse;
import com.wealthsense.common.enums.TransactionType;
import com.wealthsense.security.encryption.AESEncryptionService;
import com.wealthsense.security.masking.DataMaskingService;
import com.wealthsense.transaction.domain.Transaction;
import com.wealthsense.transaction.dto.TransactionResponse;
import com.wealthsense.transaction.mapper.TransactionMapper;
import com.wealthsense.transaction.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * CQRS read model — checks Redis cache first, falls back to PostgreSQL.
 * Cache failures are caught silently (cache must not break reads).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private static final String CACHE_KEY_PREFIX = "txn:user:";

    private final TransactionRepository transactionRepository;
    private final TransactionMapper transactionMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AESEncryptionService aesEncryptionService;
    private final DataMaskingService dataMaskingService;

    @SuppressWarnings("unchecked")
    public PagedResponse<TransactionResponse> getTransactions(
            UUID userId, int page, int size,
            TransactionType type, String category,
            Instant startDate, Instant endDate,
            long cacheTtlMinutes) {

        String cacheKey = buildCacheKey(userId, page, size, type, category);

        // Try cache first
        try {
            Object cached = redisTemplate.opsForValue().get(cacheKey);
            if (cached instanceof PagedResponse) {
                log.debug("Cache hit for key: {}", cacheKey);
                return (PagedResponse<TransactionResponse>) cached;
            }
        } catch (Exception e) {
            log.warn("Cache read failed, falling back to DB: {}", e.getMessage());
        }

        // Cache miss — query DB
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<Transaction> txnPage = transactionRepository.findByFilters(
                userId, type, category, startDate, endDate, pageRequest);

        PagedResponse<TransactionResponse> response = PagedResponse.<TransactionResponse>builder()
                .content(txnPage.getContent().stream()
                        .map(this::toMaskedResponse).toList())
                .pageNumber(txnPage.getNumber())
                .pageSize(txnPage.getSize())
                .totalElements(txnPage.getTotalElements())
                .totalPages(txnPage.getTotalPages())
                .first(txnPage.isFirst())
                .last(txnPage.isLast())
                .empty(txnPage.isEmpty())
                .build();

        // Cache result
        try {
            redisTemplate.opsForValue().set(cacheKey, response, cacheTtlMinutes, TimeUnit.MINUTES);
            log.debug("Cached result for key: {}", cacheKey);
        } catch (Exception e) {
            log.warn("Cache write failed: {}", e.getMessage());
        }

        return response;
    }

    public void invalidateUserCache(UUID userId) {
        try {
            String pattern = CACHE_KEY_PREFIX + userId + ":*";
            var keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                log.debug("Invalidated {} cache keys for user: {}", keys.size(), userId);
            }
        } catch (Exception e) {
            log.warn("Cache invalidation failed: {}", e.getMessage());
        }
    }

    private String buildCacheKey(UUID userId, int page, int size,
                                  TransactionType type, String category) {
        return CACHE_KEY_PREFIX + userId + ":page:" + page + ":size:" + size
                + (type != null ? ":type:" + type : "")
                + (category != null ? ":cat:" + category : "");
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
