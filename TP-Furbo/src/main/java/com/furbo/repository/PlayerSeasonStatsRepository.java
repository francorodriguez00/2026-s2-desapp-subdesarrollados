package com.furbo.repository;

import com.furbo.model.PlayerSeasonStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlayerSeasonStatsRepository extends JpaRepository<PlayerSeasonStats, Long> {
    Optional<PlayerSeasonStats> findByWhoscoredPlayerIdAndWhoscoredTeamIdAndTournamentIdAndSeasonId(
        Long playerId, Integer teamId, Integer tournamentId, Integer seasonId
    );

    List<PlayerSeasonStats> findByPlayerId(Long playerId);

    List<PlayerSeasonStats> findByPlayerIdIsNotNull();

    @Query("SELECT COUNT(DISTINCT s.whoscoredPlayerId) FROM PlayerSeasonStats s WHERE s.whoscoredPlayerId IS NOT NULL")
    long countDistinctWhoscoredPlayerId();

    @Query("SELECT COUNT(DISTINCT s.whoscoredPlayerId) FROM PlayerSeasonStats s WHERE s.whoscoredPlayerId IS NOT NULL AND s.playerId IS NOT NULL")
    long countDistinctWhoscoredPlayerIdLinked();

    @Query("SELECT s.whoscoredPlayerId FROM PlayerSeasonStats s GROUP BY s.whoscoredPlayerId HAVING SUM(CASE WHEN s.playerId IS NOT NULL THEN 1 ELSE 0 END) = 0 ORDER BY SUM(COALESCE(s.minsPlayed, 0)) DESC, s.whoscoredPlayerId ASC")
    List<Long> findUnlinkedWhoscoredPlayerIds(org.springframework.data.domain.Pageable pageable);

    @Query("SELECT COUNT(DISTINCT s.whoscoredPlayerId) FROM PlayerSeasonStats s WHERE s.whoscoredPlayerId NOT IN (SELECT DISTINCT s2.whoscoredPlayerId FROM PlayerSeasonStats s2 WHERE s2.playerId IS NOT NULL AND s2.whoscoredPlayerId IS NOT NULL)")
    long countUnlinkedWhoscoredPlayerIds();

    @Query(value = "SELECT st.tournament_id, p.league, COUNT(DISTINCT st.whoscored_player_id) FROM player_season_stats st JOIN player_source_id s ON s.external_id = CAST(st.whoscored_player_id AS VARCHAR) JOIN players p ON p.id = s.player_id WHERE s.source = 'WHOSCORED' GROUP BY st.tournament_id, p.league", nativeQuery = true)
    List<Object[]> findTournamentLeagueVotesNative();

    List<PlayerSeasonStats> findByWhoscoredPlayerIdIn(Collection<Long> whoscoredPlayerIds);

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
