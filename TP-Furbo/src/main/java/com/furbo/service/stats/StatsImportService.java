package com.furbo.service.stats;

import com.furbo.adapter.dto.whoscored.WhoScoredPlayerStatsDTO;
import com.furbo.adapter.dto.whoscored.WhoScoredResponseDTO;
import com.furbo.adapter.whoscored.WhoScoredClient;
import com.furbo.model.stats.PlayerSeasonStats;
import com.furbo.repository.mapping.PlayerMappingRepository;
import com.furbo.repository.stats.PlayerSeasonStatsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsImportService {

    private final WhoScoredClient client;
    private final PlayerMappingRepository mappingRepository;
    private final PlayerSeasonStatsRepository statsRepository;

    public StatsImportService(WhoScoredClient client, 
                              PlayerMappingRepository mappingRepository, 
                              PlayerSeasonStatsRepository statsRepository) {
        this.client = client;
        this.mappingRepository = mappingRepository;
        this.statsRepository = statsRepository;
    }

    @Transactional
    public void importStats(String url, String seasonId, String competition) {
        WhoScoredResponseDTO response = client.fetchStats(url);
        
        if (response.getPlayerTableStats() == null) return;

        for (WhoScoredPlayerStatsDTO dto : response.getPlayerTableStats()) {
            mappingRepository.findByWhoScoredPlayerId(dto.getPlayerId()).ifPresent(mapping -> {
                PlayerSeasonStats stats = statsRepository
                        .findByWhoScoredPlayerIdAndSeasonIdAndCompetition(dto.getPlayerId(), seasonId, competition)
                        .orElse(PlayerSeasonStats.builder()
                                .player(mapping.getLocalPlayer())
                                .whoScoredPlayerId(dto.getPlayerId())
                                .seasonId(seasonId)
                                .competition(competition)
                                .build());

                updateStats(stats, dto);
                statsRepository.save(stats);
            });
        }
    }

    private void updateStats(PlayerSeasonStats stats, WhoScoredPlayerStatsDTO dto) {
        stats.setApps(dto.getApps());
        stats.setMinsPlayed(dto.getMinsPlayed());
        stats.setGoal(dto.getGoal());
        stats.setAssistTotal(dto.getAssistTotal());
        stats.setYellowCard(dto.getYellowCard());
        stats.setRedCard(dto.getRedCard());
        stats.setShotsPerGame(dto.getShotsPerGame());
        stats.setPassSuccess(dto.getPassSuccess());
        stats.setAerialWonPerGame(dto.getAerialWonPerGame());
        stats.setManOfTheMatch(dto.getManOfTheMatch());
        stats.setRating(dto.getRating());
    }
}
