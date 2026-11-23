package com.furbo.repository;

import com.furbo.model.PlayerSourceId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PlayerSourceIdRepository extends JpaRepository<PlayerSourceId, Long> {
    Optional<PlayerSourceId> findBySourceAndExternalId(String source, String externalId);
    Optional<PlayerSourceId> findByPlayerIdAndSource(Long playerId, String source);
    List<PlayerSourceId> findAllBySource(String source);
    List<PlayerSourceId> findBySourceAndPlayerIdIn(String source, Collection<Long> playerIds);
    long countBySource(String source);
    long countDistinctPlayerIdBySource(String source);
    long countBySourceAndNeedsReview(String source, boolean needsReview);

    @Query(value = """
        SELECT s.* \
        FROM player_source_id s \
        WHERE s.source = 'WHOSCORED' AND s.needs_review = true \
          AND EXISTS (SELECT 1 FROM player_season_stats st WHERE st.whoscored_player_id = CAST(s.external_id AS BIGINT)) \
        ORDER BY COALESCE((SELECT SUM(st.mins_played) FROM player_season_stats st WHERE st.whoscored_player_id = CAST(s.external_id AS BIGINT)), 0) DESC, \
                 CAST(s.external_id AS BIGINT) ASC \
        """, 
        countQuery = """
        SELECT COUNT(s) \
        FROM player_source_id s \
        WHERE s.source = 'WHOSCORED' AND s.needs_review = true \
          AND EXISTS (SELECT 1 FROM player_season_stats st WHERE st.whoscored_player_id = CAST(s.external_id AS BIGINT)) \
        """, 
        nativeQuery = true)
    Page<PlayerSourceId> findToConfirmPaginated(Pageable pageable);

    @Query(value = "SELECT st.team_name, p.team, COUNT(DISTINCT CAST(s.external_id AS BIGINT)) FROM player_source_id s JOIN player_season_stats st ON st.whoscored_player_id = CAST(s.external_id AS BIGINT) JOIN players p ON p.id = s.player_id WHERE s.source = 'WHOSCORED' GROUP BY st.team_name, p.team", nativeQuery = true)
    List<Object[]> findTeamVotesNative();
}
