package com.wealthsense.fraud.domain;

import com.wealthsense.common.enums.FraudSeverity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "fraud_alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FraudAlert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "transaction_id", nullable = false)
    private UUID transactionId;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private FraudSeverity severity;

    @Column(name = "risk_score", precision = 3, scale = 2)
    private double riskScore;

    @Column(name = "rule_triggered")
    private String ruleTriggered;

    @Column(name = "recommended_action", length = 50)
    private String recommendedAction;

    @Column(length = 500)
    private String reason;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "text")
    @Builder.Default
    private Map<String, Object> evidence = new HashMap<>();

    @Column(length = 20)
    @Builder.Default
    private String status = "OPEN";

    @Column(name = "created_at", updatable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now();
    }
}
