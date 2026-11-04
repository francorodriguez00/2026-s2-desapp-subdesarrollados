package com.furbo.service.valuation;

import com.furbo.model.PlayerSeasonStats;
import com.furbo.repository.PlayerSeasonStatsRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlayerStatsSourceImpl implements PlayerStatsSource {

    private final PlayerSeasonStatsRepository repository;

    public PlayerStatsSourceImpl(PlayerSeasonStatsRepository repository) {
        this.repository = repository;
    }

    @Override
    public Optional<PlayerStatsSnapshot> findByPlayerId(Long playerId) {
        return repository.findAll().stream() // Simplificación: ajustar según modelo si hay campo playerId directo
                .filter(s -> s.getPlayerId().equals(playerId))
                .findFirst()
                .map(this::toSnapshot);
    }

    @Override
    public List<PlayerStatsSnapshot> findAll() {
        return repository.findAll().stream()
                .map(this::toSnapshot)
                .collect(Collectors.toList());
    }

    private PlayerStatsSnapshot toSnapshot(PlayerSeasonStats s) {
        return new PlayerStatsSnapshot(
                s.getPlayerId(),
                s.getPositionText(),
                s.getApps(),
                s.getMinsPlayed(),
                s.getGoals(),
                s.getAssists(),
                s.getYellowCards(),
                s.getRedCards(),
                s.getManOfTheMatch(),
                s.getRating(),
                s.getPassSuccess(),
                s.getKeyPassPerGame(),
                s.getDribbleWonPerGame(),
                s.getFoulGivenPerGame(),
                s.getDispossessedPerGame(),
                s.getTurnoverPerGame(),
                s.getTacklePerGame(),
                s.getInterceptionPerGame(),
                s.getFoulsPerGame(),
                s.getClearancePerGame(),
                s.getWasDribbledPerGame(),
                s.getOutfielderBlockPerGame(),
                s.getAerialWonPerGame(),
                s.getTotalPassesPerGame(),
                s.getAccurateCrossesPerGame(),
                s.getAccurateLongPassPerGame(),
                s.getAccurateThroughBallPerGame(),
                s.getUpdatedAt()
        );
    }
}
