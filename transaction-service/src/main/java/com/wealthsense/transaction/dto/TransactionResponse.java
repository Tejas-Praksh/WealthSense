package com.wealthsense.transaction.dto;

import com.wealthsense.common.enums.TransactionStatus;
import com.wealthsense.common.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponse {
    private UUID id;
    private UUID userId;
    private UUID accountId;
    private BigDecimal amount;
    private String currency;
    private TransactionType type;
    private TransactionStatus status;
    private String category;
    private String merchantName;
    private String merchantId;
    private String description;
    private Instant createdAt;
    private Instant processedAt;
    private Map<String, String> metadata;
}
