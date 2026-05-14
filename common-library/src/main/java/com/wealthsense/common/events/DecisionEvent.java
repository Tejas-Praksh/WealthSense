package com.wealthsense.common.events;

import com.wealthsense.common.enums.DecisionType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DecisionEvent extends BaseEvent {

    @Serial
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private UUID decisionId = UUID.randomUUID();
    private UUID transactionId;
    private DecisionType decision;
    private Double confidence;
    @Builder.Default
    private List<String> rulesApplied = new ArrayList<>();
    private Long processingTimeMs;
    private String explanation;
}
