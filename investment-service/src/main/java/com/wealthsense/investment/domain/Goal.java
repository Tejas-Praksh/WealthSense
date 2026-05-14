package com.wealthsense.investment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "goals")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private String name;

    @Column(precision = 19, scale = 4)
    private BigDecimal targetAmountPaise;

    @Column(precision = 19, scale = 4)
    private BigDecimal currentAmountPaise;

    @Column(precision = 19, scale = 4)
    private BigDecimal monthlySavingPaise;

    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private GoalStatus status;

    private Integer priority;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
