package com.furbo.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "player_season_stats", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"whoscored_player_id", "whoscored_team_id", "tournament_id", "season_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlayerSeasonStats {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long whoscoredPlayerId;
    private Integer whoscoredTeamId;
    private Integer tournamentId;
    private Integer seasonId;

    private Long playerId; // FK

    private String name;
    private String firstName;
    private String lastName;
    private Integer age;
    private String positionText;
    private String playedPositions;
    private String teamName;
    private String tournamentName;
    private String seasonName;
    private Integer height;
    private Integer weight;

    private Integer apps;
    private Integer subOn;
    private Integer minsPlayed;
    private Double rating;

    // Summary
    private Integer goals;
    private Integer assists;
    private Integer yellowCards;
    private Integer redCards;
    private Double shotsPerGame;
    private Double passSuccess;
    private Double aerialWonPerGame;
    private Boolean manOfTheMatch;

    // Offensive
    private Double keyPassPerGame;
    private Double dribbleWonPerGame;
    private Double foulGivenPerGame;
    private Double dispossessedPerGame;
    private Double turnoverPerGame;

    // Defensive
    private Double tacklePerGame;
    private Double interceptionPerGame;
    private Double foulsPerGame;
    private Double clearancePerGame;
    private Double wasDribbledPerGame;
    private Double outfielderBlockPerGame;

    // Passing
    private Double totalPassesPerGame;
    private Double accurateCrossesPerGame;
    private Double accurateLongPassPerGame;
    private Double accurateThroughBallPerGame;

    private LocalDateTime fetchedAt;
    private LocalDateTime updatedAt;
}
