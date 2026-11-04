package com.furbo.repository;

import com.furbo.model.PlayerSourceId;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlayerSourceIdRepository extends JpaRepository<PlayerSourceId, Long> {
    Optional<PlayerSourceId> findBySourceAndExternalId(String source, String externalId);
    Optional<PlayerSourceId> findByPlayerIdAndSource(Long playerId, String source);
    List<PlayerSourceId> findAllBySource(String source);
    long countBySource(String source);
    long countDistinctPlayerIdBySource(String source);
    @org.springframework.data.jpa.repository.Query(value = "SELECT st.team_name, p.team, COUNT(DISTINCT CAST(s.external_id AS BIGINT)) FROM player_source_id s JOIN player_season_stats st ON st.whoscored_player_id = CAST(s.external_id AS BIGINT) JOIN players p ON p.id = s.player_id WHERE s.source = 'WHOSCORED' GROUP BY st.team_name, p.team", nativeQuery = true)
    List<Object[]> findTeamVotesNative();
}
