package com.furbo.service.matching;

import com.furbo.model.PlayerMatchReview;
import com.furbo.model.PlayerSourceId;
import com.furbo.model.ReviewStatus;
import com.furbo.repository.PlayerMatchReviewRepository;
import com.furbo.repository.PlayerSourceIdRepository;
import com.furbo.repository.PlayerSeasonStatsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

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

    public Set<Long> getRejectedCandidateIds(String source, String externalId) {
        List<PlayerMatchReview> rejected = reviewRepository.findRejectedBySourceAndExternalId(source, externalId);
        Set<Long> ids = new HashSet<>();
        for (PlayerMatchReview r : rejected) {
            ids.add(r.getCandidatePlayerId());
        }
        return ids;
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
        Set<Long> rejectedIds = getRejectedCandidateIds(source, externalId);
        List<MatchOutcome.Review.Candidate> filtered = new ArrayList<>();
        for (MatchOutcome.Review.Candidate c : candidates) {
            if (!rejectedIds.contains(c.playerId())) {
                filtered.add(c);
            }
        }

        if (filtered.isEmpty()) {
            reviewRepository.deletePendingBySourceAndExternalId(source, externalId);
            return;
        }

        List<Long> validCandidateIds = new ArrayList<>();
        for (MatchOutcome.Review.Candidate c : filtered) {
            validCandidateIds.add(c.playerId());
            List<PlayerMatchReview> existingList = reviewRepository.findBySourceAndExternalIdAndCandidatePlayerId(source, externalId, c.playerId());
            if (!existingList.isEmpty()) {
                PlayerMatchReview rev = existingList.get(0);
                if (existingList.size() > 1) {
                    for (int i = 1; i < existingList.size(); i++) {
                        reviewRepository.delete(existingList.get(i));
                    }
                }
                if (rev.getStatus() == ReviewStatus.REJECTED) {
                    continue;
                }
                rev.setScore(c.score());
                rev.setReason(c.reason());
                if (rev.getStatus() == null) {
                    rev.setStatus(ReviewStatus.PENDING);
                }
                reviewRepository.save(rev);
            } else {
                PlayerMatchReview rev = PlayerMatchReview.builder()
                    .source(source)
                    .externalId(externalId)
                    .candidatePlayerId(c.playerId())
                    .score(c.score())
                    .reason(c.reason())
                    .status(ReviewStatus.PENDING)
                    .createdAt(LocalDateTime.now())
                    .build();
                reviewRepository.save(rev);
            }
        }
        if (!validCandidateIds.isEmpty()) {
            reviewRepository.deletePendingNotIn(source, externalId, validCandidateIds);
        }
    }

    @Transactional
    public void deleteReviews(String source, String externalId) {
        reviewRepository.deleteBySourceAndExternalId(source, externalId);
    }

    @Transactional
    public void deletePendingReviews(String source, String externalId) {
        reviewRepository.deletePendingBySourceAndExternalId(source, externalId);
    }
}
