package com.furbo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long buyerId;
    private Long sellerId;
    private Long playerId;
    private Integer quantity;
    @Column(precision = 19, scale = 2)
    private BigDecimal unitPrice;
    @Column(precision = 19, scale = 2)
    private BigDecimal commission = BigDecimal.ZERO;
    private String operationId;
    private LocalDateTime createdAt = LocalDateTime.now();
}
