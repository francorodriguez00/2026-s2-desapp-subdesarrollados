package com.furbo.service.matching;

import com.furbo.adapter.dto.LinkActionResultDTO;
import com.furbo.model.*;
import com.furbo.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class PlayerLinkAdminService {

    private final PlayerSourceIdRepository sourceRepository;
    private final PlayerMatchReviewRepository reviewRepository;
    private final PlayerSeasonStatsRepository statsRepository;
    private final PlayerRepository playerRepository;
    private final PlayerMatchingPersister persister;

    public PlayerLinkAdminService(PlayerSourceIdRepository sourceRepository,
                                  PlayerMatchReviewRepository reviewRepository,
                                  PlayerSeasonStatsRepository statsRepository,
                                  PlayerRepository playerRepository,
                                  PlayerMatchingPersister persister) {
        this.sourceRepository = sourceRepository;
        this.reviewRepository = reviewRepository;
        this.statsRepository = statsRepository;
        this.playerRepository = playerRepository;
        this.persister = persister;
    }

    private Long parseExternalId(String externalId) {
        try {
            return Long.parseLong(externalId);
        } catch (NumberFormatException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "External ID must be numeric: " + externalId);
        }
    }

    private void validateStatsExist(Long wsId) {
        List<PlayerSeasonStats> stats = statsRepository.findByWhoscoredPlayerId(wsId);
        if (stats.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No stats found for WhoScored player ID: " + wsId);
        }
    }

    private void validatePlayerExist(Long playerId) {
        if (!playerRepository.existsById(playerId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Player not found with ID: " + playerId);
        }
    }

    @Transactional
    public LinkActionResultDTO linkManually(String externalId, Long playerId) {
        Long wsId = parseExternalId(externalId);
        validateStatsExist(wsId);
        validatePlayerExist(playerId);

        Optional<PlayerSourceId> existingByExt = sourceRepository.findBySourceAndExternalId("WHOSCORED", externalId);
        if (existingByExt.isPresent()) {
            Long currentLinkedPlayerId = existingByExt.get().getPlayerId();
            if (currentLinkedPlayerId.equals(playerId)) {
                return new LinkActionResultDTO(externalId, "ALREADY_LINKED_SAME", playerId, playerId, 0);
            } else {
                throw new ResponseStatusException(HttpStatus.CONFLICT, 
                    "External ID " + externalId + " is already linked to player ID " + currentLinkedPlayerId);
            }
        }

        Optional<PlayerSourceId> existingByPlayer = sourceRepository.findByPlayerIdAndSource(playerId, "WHOSCORED");
        if (existingByPlayer.isPresent()) {
            String ownerExtId = existingByPlayer.get().getExternalId();
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                "Player ID " + playerId + " is already linked to WhoScored external ID " + ownerExtId);
        }

        LinkResult res = persister.link(externalId, playerId, "MANUAL", 1.0, false);
        if (res == LinkResult.CONFLICT) {
            Optional<PlayerSourceId> conflictPlayer = sourceRepository.findByPlayerIdAndSource(playerId, "WHOSCORED");
            String owner = conflictPlayer.map(PlayerSourceId::getExternalId).orElse("unknown");
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Player ID " + playerId + " is already linked to external ID " + owner);
        }

        statsRepository.setPlayerIdForWhoscoredId(wsId, playerId);
        persister.deleteReviews("WHOSCORED", externalId);

        List<PlayerSeasonStats> updatedStats = statsRepository.findByWhoscoredPlayerId(wsId);
        return new LinkActionResultDTO(externalId, "LINKED", playerId, null, updatedStats.size());
    }

    @Transactional
    public LinkActionResultDTO unlink(String externalId) {
        Long wsId = parseExternalId(externalId);
        Optional<PlayerSourceId> linkOpt = sourceRepository.findBySourceAndExternalId("WHOSCORED", externalId);
        if (linkOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active link found for external ID: " + externalId);
        }

        PlayerSourceId link = linkOpt.get();
        Long previousPlayerId = link.getPlayerId();

        sourceRepository.delete(link);
        statsRepository.clearPlayerIdForWhoscoredId(wsId);

        List<PlayerMatchReview> existingRevList = reviewRepository.findBySourceAndExternalIdAndCandidatePlayerId("WHOSCORED", externalId, previousPlayerId);
        if (!existingRevList.isEmpty()) {
            PlayerMatchReview rev = existingRevList.get(0);
            rev.setStatus(ReviewStatus.REJECTED);
            rev.setReason("UNLINKED");
            reviewRepository.save(rev);
        } else {
            PlayerMatchReview rev = PlayerMatchReview.builder()
                .source("WHOSCORED")
                .externalId(externalId)
                .candidatePlayerId(previousPlayerId)
                .score(1.0)
                .reason("UNLINKED")
                .status(ReviewStatus.REJECTED)
                .createdAt(LocalDateTime.now())
                .build();
            reviewRepository.save(rev);
        }

        List<PlayerSeasonStats> updatedStats = statsRepository.findByWhoscoredPlayerId(wsId);
        return new LinkActionResultDTO(externalId, "UNLINKED", null, previousPlayerId, updatedStats.size());
    }

    @Transactional
    public LinkActionResultDTO confirm(String externalId) {
        parseExternalId(externalId);
        Optional<PlayerSourceId> linkOpt = sourceRepository.findBySourceAndExternalId("WHOSCORED", externalId);
        if (linkOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No active link found for external ID: " + externalId);
        }

        PlayerSourceId link = linkOpt.get();
        link.setNeedsReview(false);
        sourceRepository.save(link);

        return new LinkActionResultDTO(externalId, "CONFIRMED", link.getPlayerId(), link.getPlayerId(), 0);
    }

    @Transactional
    public LinkActionResultDTO reject(String externalId, Long candidatePlayerId) {
        parseExternalId(externalId);
        List<PlayerMatchReview> pending;
        if (candidatePlayerId != null) {
            List<PlayerMatchReview> revList = reviewRepository.findBySourceAndExternalIdAndCandidatePlayerId("WHOSCORED", externalId, candidatePlayerId);
            if (revList.isEmpty() || (revList.get(0).getStatus() != null && revList.get(0).getStatus() != ReviewStatus.PENDING)) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending review found for candidate ID " + candidatePlayerId + " and external ID " + externalId);
            }
            pending = List.of(revList.get(0));
        } else {
            pending = reviewRepository.findPendingBySourceAndExternalId("WHOSCORED", externalId);
            if (pending.isEmpty()) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No pending reviews found for external ID: " + externalId);
            }
        }

        for (PlayerMatchReview rev : pending) {
            rev.setStatus(ReviewStatus.REJECTED);
            reviewRepository.save(rev);
        }

        return new LinkActionResultDTO(externalId, "REJECTED", null, null, 0);
    }

    @Transactional
    public LinkActionResultDTO approve(String externalId, Long candidatePlayerId) {
        if (candidatePlayerId == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidate player ID is required for approval");
        }
        parseExternalId(externalId);
        List<PlayerMatchReview> pending = reviewRepository.findPendingBySourceAndExternalId("WHOSCORED", externalId);
        boolean found = false;
        for (PlayerMatchReview r : pending) {
            if (r.getCandidatePlayerId().equals(candidatePlayerId)) {
                found = true;
                break;
            }
        }
        if (!found) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Candidate player ID " + candidatePlayerId + " is not among pending reviews for external ID " + externalId);
        }

        return linkManually(externalId, candidatePlayerId);
    }
}
