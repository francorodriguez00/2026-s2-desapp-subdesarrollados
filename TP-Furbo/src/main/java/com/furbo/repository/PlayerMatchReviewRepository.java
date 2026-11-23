package com.furbo.repository;

import com.furbo.model.PlayerMatchReview;
import com.furbo.model.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface PlayerMatchReviewRepository extends JpaRepository<PlayerMatchReview, Long> {
    List<PlayerMatchReview> findBySourceAndExternalIdAndCandidatePlayerId(String source, String externalId, Long candidateId);
    
    List<PlayerMatchReview> findBySourceAndExternalId(String source, String externalId);

    @Query("SELECT r FROM PlayerMatchReview r WHERE r.source = :source AND r.externalId = :externalId AND r.status = com.furbo.model.ReviewStatus.REJECTED")
    List<PlayerMatchReview> findRejectedBySourceAndExternalId(@Param("source") String source, @Param("externalId") String externalId);

    @Query("SELECT r FROM PlayerMatchReview r WHERE r.source = :source AND r.externalId = :externalId AND (r.status IS NULL OR r.status = com.furbo.model.ReviewStatus.PENDING)")
    List<PlayerMatchReview> findPendingBySourceAndExternalId(@Param("source") String source, @Param("externalId") String externalId);

    @Modifying
    @Query("DELETE FROM PlayerMatchReview r WHERE r.source = :source AND r.externalId = :externalId AND (r.status IS NULL OR r.status = com.furbo.model.ReviewStatus.PENDING) AND r.candidatePlayerId NOT IN :candidatePlayerIds")
    void deletePendingNotIn(@Param("source") String source, @Param("externalId") String externalId, @Param("candidatePlayerIds") Collection<Long> candidatePlayerIds);

    @Modifying
    @Query("DELETE FROM PlayerMatchReview r WHERE r.source = :source AND r.externalId = :externalId AND (r.status IS NULL OR r.status = com.furbo.model.ReviewStatus.PENDING)")
    void deletePendingBySourceAndExternalId(@Param("source") String source, @Param("externalId") String externalId);

    void deleteBySourceAndExternalIdAndCandidatePlayerIdNotIn(String source, String externalId, Collection<Long> candidatePlayerIds);
    
    void deleteBySourceAndExternalId(String source, String externalId);
}
