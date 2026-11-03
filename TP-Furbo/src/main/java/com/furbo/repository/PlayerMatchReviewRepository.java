package com.furbo.repository;

import com.furbo.model.PlayerMatchReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PlayerMatchReviewRepository extends JpaRepository<PlayerMatchReview, Long> {
    Optional<PlayerMatchReview> findBySourceAndExternalIdAndCandidatePlayerId(String source, String externalId, Long candidateId);
    List<PlayerMatchReview> findBySourceAndExternalId(String source, String externalId);
}
