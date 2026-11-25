package com.furbo.service.valuation;

import com.furbo.model.PlayerSeasonStats;
import com.furbo.repository.PlayerSeasonStatsRepository;
import org.springframework.stereotype.Service;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class PlayerStatsSourceImpl implements PlayerStatsSource {

    private final PlayerSeasonStatsRepository repository;

    public PlayerStatsSourceImpl(PlayerSeasonStatsRepository repository) {
        this.repository = repository;
    }

    private PlayerSeasonStats pickBest(List<PlayerSeasonStats> statsList) {
        if (statsList == null || statsList.isEmpty()) {
            return null;
        }
        return statsList.stream()
                .max(Comparator.comparingInt((PlayerSeasonStats s) -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0)
                        .thenComparing(s -> s.getUpdatedAt() != null ? s.getUpdatedAt() : java.time.LocalDateTime.MIN)
                        .thenComparing(s -> s.getId() != null ? s.getId() : 0L))
                .orElse(statsList.get(0));
    }

    @Override
    public Optional<PlayerStatsSnapshot> findByPlayerId(Long playerId) {
        if (playerId == null) {
            return Optional.empty();
        }
        List<PlayerSeasonStats> rows = repository.findByPlayerId(playerId);
        PlayerSeasonStats best = pickBest(rows);
        if (best == null) {
            return Optional.empty();
        }
        return Optional.of(toSnapshot(best));
    }

    @Override
    public List<PlayerStatsSnapshot> findAll() {
        List<PlayerSeasonStats> validRows = repository.findByPlayerIdIsNotNull();
        Map<Long, List<PlayerSeasonStats>> grouped = validRows.stream()
                .filter(s -> s.getPlayerId() != null)
                .collect(Collectors.groupingBy(PlayerSeasonStats::getPlayerId));

        return grouped.values().stream()
                .map(this::pickBest)
                .filter(java.util.Objects::nonNull)
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
