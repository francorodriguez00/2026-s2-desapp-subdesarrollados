package com.furbo.repository;

import com.furbo.model.PlayerSeasonStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PlayerSeasonStatsRepository extends JpaRepository<PlayerSeasonStats, Long> {
    Optional<PlayerSeasonStats> findByWhoscoredPlayerIdAndWhoscoredTeamIdAndTournamentIdAndSeasonId(
        Long playerId, Integer teamId, Integer tournamentId, Integer seasonId
    );

    List<PlayerSeasonStats> findByPlayerId(Long playerId);

    List<PlayerSeasonStats> findByPlayerIdIsNotNull();

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PlayerSeasonStats s SET s.playerId = :playerId WHERE s.whoscoredPlayerId = :whoscoredId")
    void setPlayerIdForWhoscoredId(@Param("whoscoredId") Long whoscoredId, @Param("playerId") Long playerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE PlayerSeasonStats s SET s.playerId = null WHERE s.whoscoredPlayerId = :whoscoredId")
    void clearPlayerIdForWhoscoredId(@Param("whoscoredId") Long whoscoredId);

    @Modifying
    @Query("UPDATE PlayerSeasonStats s SET s.playerId = :playerId WHERE s.whoscoredPlayerId = :whoscoredId AND s.playerId IS NULL")
    void updatePlayerIdByWhoscoredId(@Param("playerId") Long playerId, @Param("whoscoredId") Long whoscoredId);
}
