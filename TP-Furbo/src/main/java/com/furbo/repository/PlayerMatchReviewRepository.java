package com.furbo.repository;

import com.furbo.model.PlayerMatchReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlayerMatchReviewRepository extends JpaRepository<PlayerMatchReview, Long> {
    Optional<PlayerMatchReview> findBySourceAndExternalIdAndCandidatePlayerId(String source, String externalId, Long candidateId);
    List<PlayerMatchReview> findBySourceAndExternalId(String source, String externalId);
    void deleteBySourceAndExternalIdAndCandidatePlayerIdNotIn(String source, String externalId, java.util.Collection<Long> candidatePlayerIds);
    void deleteBySourceAndExternalId(String source, String externalId);
}
