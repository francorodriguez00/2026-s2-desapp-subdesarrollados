package com.furbo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_match_review")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerMatchReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String source;
    private String externalId;
    private Long candidatePlayerId;
    private Double score;
    private String reason;
    private LocalDateTime createdAt;
}
