package com.furbo.repository;

import com.furbo.model.PlayerMatchReview;
import com.furbo.model.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("SELECT r FROM PlayerMatchReview r WHERE r.source = :source AND r.externalId IN :externalIds AND (r.status IS NULL OR r.status = com.furbo.model.ReviewStatus.PENDING)")
    List<PlayerMatchReview> findPendingBySourceAndExternalIdIn(@Param("source") String source, @Param("externalIds") Collection<String> externalIds);

    @Query(value = """
        SELECT r.external_id \
        FROM player_match_review r \
        JOIN player_season_stats st ON st.whoscored_player_id = CAST(r.external_id AS BIGINT) \
        WHERE r.source = 'WHOSCORED' \
          AND (r.status IS NULL OR r.status = 'PENDING') \
          AND r.external_id NOT IN (SELECT s.external_id FROM player_source_id s WHERE s.source = 'WHOSCORED') \
        GROUP BY r.external_id \
        ORDER BY SUM(st.mins_played) DESC, CAST(r.external_id AS BIGINT) ASC \
        """, 
        countQuery = """
        SELECT COUNT(DISTINCT r.external_id) \
        FROM player_match_review r \
        WHERE r.source = 'WHOSCORED' \
          AND (r.status IS NULL OR r.status = 'PENDING') \
          AND r.external_id NOT IN (SELECT s.external_id FROM player_source_id s WHERE s.source = 'WHOSCORED') \
        """, 
        nativeQuery = true)
    Page<String> findPendingUnlinkedExternalIds(Pageable pageable);

    @Query(value = """
        SELECT COUNT(DISTINCT r.external_id) \
        FROM player_match_review r \
        WHERE r.source = 'WHOSCORED' \
          AND (r.status IS NULL OR r.status = 'PENDING') \
          AND r.external_id NOT IN (SELECT s.external_id FROM player_source_id s WHERE s.source = 'WHOSCORED') \
        """, nativeQuery = true)
    long countPendingUnlinkedExternalIds();

    @Modifying
    @Query("DELETE FROM PlayerMatchReview r WHERE r.source = :source AND r.externalId = :externalId AND (r.status IS NULL OR r.status = com.furbo.model.ReviewStatus.PENDING) AND r.candidatePlayerId NOT IN :candidatePlayerIds")
    void deletePendingNotIn(@Param("source") String source, @Param("externalId") String externalId, @Param("candidatePlayerIds") Collection<Long> candidatePlayerIds);

    @Modifying
    @Query("DELETE FROM PlayerMatchReview r WHERE r.source = :source AND r.externalId = :externalId AND (r.status IS NULL OR r.status = com.furbo.model.ReviewStatus.PENDING)")
    void deletePendingBySourceAndExternalId(@Param("source") String source, @Param("externalId") String externalId);

    void deleteBySourceAndExternalIdAndCandidatePlayerIdNotIn(String source, String externalId, Collection<Long> candidatePlayerIds);
    
    void deleteBySourceAndExternalId(String source, String externalId);
}
