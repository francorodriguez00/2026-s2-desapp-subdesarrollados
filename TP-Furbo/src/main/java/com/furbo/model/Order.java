package com.furbo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_order_player_status_side_price", columnList = "playerId, status, side, price, createdAt"),
    @Index(name = "idx_order_user_status", columnList = "userId, status")
})
@Getter
@Setter
@NoArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long playerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderSide side;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer quantityOriginal;

    @Column(nullable = false)
    private Integer quantityRemaining;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal reservedAmount = BigDecimal.ZERO;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum OrderSide {
        BUY, SELL
    }

    public enum OrderType {
        LIMIT, MARKET
    }

    public enum OrderStatus {
        OPEN, PARTIAL, FILLED, CANCELLED
    }
}
