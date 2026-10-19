package com.furbo.repository.stats;

import com.furbo.model.stats.PlayerSeasonStats;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerSeasonStatsRepository extends JpaRepository<PlayerSeasonStats, Long> {
    Optional<PlayerSeasonStats> findByWhoScoredPlayerIdAndSeasonIdAndCompetition(
            Long whoScoredPlayerId, String seasonId, String competition);
}
