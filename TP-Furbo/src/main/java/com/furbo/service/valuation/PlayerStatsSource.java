package com.furbo.service.valuation;

import java.util.List;
import java.util.Optional;

public interface PlayerStatsSource {
    Optional<PlayerStatsSnapshot> findByPlayerId(Long playerId);
    List<PlayerStatsSnapshot> findAll();
}
