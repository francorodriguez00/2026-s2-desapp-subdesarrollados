package com.furbo.model.stats;

import com.furbo.model.Player;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "player_season_stats", indexes = {
    @Index(columnList = "player_id,season_id,competition", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSeasonStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private Player player;

    @Column(name = "whoscored_player_id", nullable = false)
    private Long whoScoredPlayerId;

    @Column(name = "season_id", nullable = false)
    private String seasonId;
    
    @Column(name = "season_name")
    private String seasonName;

    @Column(nullable = false)
    private String competition;

    private Integer apps;
    private Integer minsPlayed;
    private Integer goal;
    private Integer assistTotal;
    private Integer yellowCard;
    private Integer redCard;
    private Double shotsPerGame;
    private Double passSuccess;
    private Double aerialWonPerGame;
    private Integer manOfTheMatch;
    private Double rating;
}
