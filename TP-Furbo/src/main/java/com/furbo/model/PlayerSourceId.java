package com.furbo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_source_id", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"source", "external_id"}),
    @UniqueConstraint(columnNames = {"player_id", "source"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSourceId {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "player_id", nullable = false)
    private Long playerId;

    @Column(nullable = false)
    private String source;

    @Column(name = "external_id", nullable = false)
    private String externalId;

    private String matchMethod;

    @Column(precision = 4, scale = 3)
    private java.math.BigDecimal confidence;

    @Builder.Default
    private boolean needsReview = false;

    private LocalDateTime createdAt;
}
