package com.furbo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trades", indexes = {
    @Index(name = "idx_trade_player", columnList = "playerId, createdAt")
})
@Getter
@Setter
@NoArgsConstructor
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long buyerOrderId;

    @Column(nullable = false)
    private Long sellerOrderId;

    @Column(nullable = false)
    private Long playerId;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal commissionBuyer;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal commissionSeller;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
