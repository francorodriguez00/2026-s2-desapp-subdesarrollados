package com.furbo.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sell_offers", indexes = {
    @Index(name = "idx_sell_offer_player_status", columnList = "playerId, status, createdAt")
})
@Getter
@Setter
@NoArgsConstructor
public class SellOffer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private Long playerId;

    @Column(nullable = false)
    private Integer quantityTotal;

    @Column(nullable = false)
    private Integer quantityRemaining;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OfferStatus status;

    private LocalDateTime createdAt = LocalDateTime.now();

    public enum OfferStatus {
        OPEN, FILLED, CANCELLED
    }
}
