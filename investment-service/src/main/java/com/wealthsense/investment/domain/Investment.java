package com.wealthsense.investment.domain;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "investments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Investment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    private String name;

    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private InvestmentType type;

    @Column(precision = 19, scale = 4)
    private BigDecimal amountPaise;

    @Column(precision = 19, scale = 4)
    private BigDecimal monthlySipPaise;

    @Column(precision = 5, scale = 2)
    private BigDecimal expectedReturnRate;

    private LocalDate startDate;
    private LocalDate endDate;

    @Column(precision = 19, scale = 4)
    private BigDecimal currentValuePaise;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private InvestmentStatus status;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
