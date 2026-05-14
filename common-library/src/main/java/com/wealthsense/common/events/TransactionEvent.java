package com.wealthsense.common.events;

import com.wealthsense.common.enums.TransactionStatus;
import com.wealthsense.common.enums.TransactionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionEvent extends BaseEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID transactionId;
    private BigDecimal amount;
    @Builder.Default
    private String currency = "INR";
    private TransactionType type;
    private String category;
    private String merchantName;
    private String merchantId;
    private UUID accountId;
    private TransactionStatus status;
    @Builder.Default
    private Map<String, String> metadata = new HashMap<>();
}
