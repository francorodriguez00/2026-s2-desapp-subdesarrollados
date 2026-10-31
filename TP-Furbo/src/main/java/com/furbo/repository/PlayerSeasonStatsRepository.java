package com.furbo.repository;

import com.furbo.model.PlayerSeasonStats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlayerSeasonStatsRepository extends JpaRepository<PlayerSeasonStats, Long> {
    Optional<PlayerSeasonStats> findByWhoscoredPlayerIdAndWhoscoredTeamIdAndTournamentIdAndSeasonId(
        Long playerId, Integer teamId, Integer tournamentId, Integer seasonId
    );

    @Modifying
    @Query("UPDATE PlayerSeasonStats s SET s.playerId = :playerId WHERE s.whoscoredPlayerId = :whoscoredId AND s.playerId IS NULL")
    void updatePlayerIdByWhoscoredId(@Param("playerId") Long playerId, @Param("whoscoredId") Long whoscoredId);
}
