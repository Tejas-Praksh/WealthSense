package com.wealthsense.common.events;

import com.wealthsense.common.enums.FraudSeverity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudAlertEvent extends BaseEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    private UUID transactionId;
    @Builder.Default
    private UUID alertId = UUID.randomUUID();
    private FraudSeverity severity;
    private String ruleTriggered;
    private Double riskScore;
    private String recommendedAction;
    private String reason;
    @Builder.Default
    private Map<String, Object> evidence = new HashMap<>();
}
