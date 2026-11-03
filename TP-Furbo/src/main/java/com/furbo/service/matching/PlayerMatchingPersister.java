package com.furbo.service.matching;

import com.furbo.model.PlayerMatchReview;
import com.furbo.model.PlayerSourceId;
import com.furbo.model.PlayerSeasonStats;
import com.furbo.repository.PlayerMatchReviewRepository;
import com.furbo.repository.PlayerSourceIdRepository;
import com.furbo.repository.PlayerSeasonStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class PlayerMatchingPersister {

    private final PlayerSourceIdRepository sourceRepository;
    private final PlayerMatchReviewRepository reviewRepository;
    private final PlayerSeasonStatsRepository statsRepository;

    public PlayerMatchingPersister(PlayerSourceIdRepository sourceRepository, 
                                   PlayerMatchReviewRepository reviewRepository,
                                   PlayerSeasonStatsRepository statsRepository) {
        this.sourceRepository = sourceRepository;
        this.reviewRepository = reviewRepository;
        this.statsRepository = statsRepository;
    }

    public Optional<PlayerSourceId> findExisting(String externalId) {
        return sourceRepository.findBySourceAndExternalId("WHOSCORED", externalId);
    }

    @Transactional
    public LinkResult link(String externalId, Long playerId, String method, double confidence) {
        Optional<PlayerSourceId> byExternalId = sourceRepository.findBySourceAndExternalId("WHOSCORED", externalId);
        if (byExternalId.isPresent()) {
            return LinkResult.ALREADY_LINKED;
        }

        Optional<PlayerSourceId> byPlayerId = sourceRepository.findByPlayerIdAndSource(playerId, "WHOSCORED");
        if (byPlayerId.isPresent()) {
            log.warn("CONFLICT: Player {} already linked to externalId {} but attempted link to {}", playerId, byPlayerId.get().getExternalId(), externalId);
            return LinkResult.CONFLICT;
        }

        PlayerSourceId link = PlayerSourceId.builder()
            .source("WHOSCORED")
            .externalId(externalId)
            .playerId(playerId)
            .matchMethod(method)
            .confidence(java.math.BigDecimal.valueOf(confidence).setScale(3, java.math.RoundingMode.HALF_UP))
            .needsReview(false)
            .createdAt(LocalDateTime.now())
            .build();
        sourceRepository.save(link);
        return LinkResult.CREATED;
    }
}
