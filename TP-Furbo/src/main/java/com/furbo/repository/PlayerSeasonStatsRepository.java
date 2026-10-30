package com.furbo.repository;

import com.furbo.model.PlayerSeasonStats;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PlayerSeasonStatsRepository extends JpaRepository<PlayerSeasonStats, Long> {
    Optional<PlayerSeasonStats> findByWhoscoredPlayerIdAndWhoscoredTeamIdAndTournamentIdAndSeasonId(
        Long playerId, Integer teamId, Integer tournamentId, Integer seasonId
    );
}
