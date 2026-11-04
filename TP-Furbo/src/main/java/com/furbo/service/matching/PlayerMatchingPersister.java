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
import java.util.List;
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

    public Optional<PlayerSourceId> findExisting(String source, String externalId) {
        return sourceRepository.findBySourceAndExternalId(source, externalId);
    }

    public Optional<PlayerSourceId> findExistingByPlayerId(Long playerId) {
        return sourceRepository.findByPlayerIdAndSource(playerId, "WHOSCORED");
    }

    @Transactional
    public LinkResult link(String externalId, Long playerId, String method, double confidence) {
        return link(externalId, playerId, method, confidence, false);
    }

    @Transactional
    public LinkResult link(String externalId, Long playerId, String method, double confidence, boolean needsReview) {
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
            .needsReview(needsReview)
            .createdAt(LocalDateTime.now())
            .build();
        sourceRepository.save(link);
        return LinkResult.CREATED;
    }

    @Transactional
    public void saveReviews(String source, String externalId, List<MatchOutcome.Review.Candidate> candidates) {
        List<Long> candidateIds = new java.util.ArrayList<>();
        for (MatchOutcome.Review.Candidate c : candidates) {
            candidateIds.add(c.playerId());
            Optional<PlayerMatchReview> existing = reviewRepository.findBySourceAndExternalIdAndCandidatePlayerId(source, externalId, c.playerId());
            if (existing.isPresent()) {
                PlayerMatchReview rev = existing.get();
                rev.setScore(c.score());
                rev.setReason(c.reason());
                reviewRepository.save(rev);
            } else {
                PlayerMatchReview rev = PlayerMatchReview.builder()
                    .source(source)
                    .externalId(externalId)
                    .candidatePlayerId(c.playerId())
                    .score(c.score())
                    .reason(c.reason())
                    .createdAt(LocalDateTime.now())
                    .build();
                reviewRepository.save(rev);
            }
        }
        if (!candidateIds.isEmpty()) {
            reviewRepository.deleteBySourceAndExternalIdAndCandidatePlayerIdNotIn(source, externalId, candidateIds);
        }
    }

    @Transactional
    public void deleteReviews(String source, String externalId) {
        reviewRepository.deleteBySourceAndExternalId(source, externalId);
    }
}
