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
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlayerLinkAdminService {

    private final PlayerSourceIdRepository sourceRepository;
    private final PlayerMatchReviewRepository reviewRepository;
    private final PlayerSeasonStatsRepository statsRepository;
    private final PlayerRepository playerRepository;
    private final PlayerMatchingPersister persister;
    private final PlayerMatcherService matcherService;

    public PlayerLinkAdminService(PlayerSourceIdRepository sourceRepository,
                                  PlayerMatchReviewRepository reviewRepository,
                                  PlayerSeasonStatsRepository statsRepository,
                                  PlayerRepository playerRepository,
                                  PlayerMatchingPersister persister,
                                  PlayerMatcherService matcherService) {
        this.sourceRepository = sourceRepository;
        this.reviewRepository = reviewRepository;
        this.statsRepository = statsRepository;
        this.playerRepository = playerRepository;
        this.persister = persister;
        this.matcherService = matcherService;
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

    @Transactional(readOnly = true)
    public com.furbo.adapter.dto.PagedReviewItemsDTO getPendingReviews(int page, int size) {
        int normSize = Math.min(Math.max(size, 1), 200);
        int normPage = Math.max(page, 0);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(normPage, normSize);
        org.springframework.data.domain.Page<String> wsIdsPage = reviewRepository.findPendingUnlinkedExternalIds(pageable);
        List<String> externalIds = wsIdsPage.getContent();
        long totalElements = wsIdsPage.getTotalElements();
        int totalPages = wsIdsPage.getTotalPages();

        if (externalIds.isEmpty()) {
            return new com.furbo.adapter.dto.PagedReviewItemsDTO(List.of(), normPage, normSize, totalElements, totalPages);
        }

        List<Long> wsIdsLong = externalIds.stream().map(Long::parseLong).toList();
        List<PlayerSeasonStats> stats = statsRepository.findByWhoscoredPlayerIdIn(wsIdsLong);
        Map<Long, List<PlayerSeasonStats>> statsGrouped = stats.stream()
            .collect(java.util.stream.Collectors.groupingBy(PlayerSeasonStats::getWhoscoredPlayerId));

        List<PlayerMatchReview> reviews = reviewRepository.findPendingBySourceAndExternalIdIn("WHOSCORED", externalIds);
        Map<String, List<PlayerMatchReview>> reviewsGrouped = reviews.stream()
            .collect(java.util.stream.Collectors.groupingBy(PlayerMatchReview::getExternalId));

        Set<Long> candidatePlayerIds = reviews.stream()
            .map(PlayerMatchReview::getCandidatePlayerId)
            .collect(java.util.stream.Collectors.toSet());

        Map<Long, Player> playersMap = playerRepository.findAllById(candidatePlayerIds).stream()
            .collect(java.util.stream.Collectors.toMap(Player::getId, p -> p));

        Map<Long, String> playerToWsIdMap = sourceRepository.findBySourceAndPlayerIdIn("WHOSCORED", candidatePlayerIds).stream()
            .collect(java.util.stream.Collectors.toMap(PlayerSourceId::getPlayerId, PlayerSourceId::getExternalId, (e1, e2) -> e1));

        List<com.furbo.adapter.dto.ReviewItemGroupDTO> content = new ArrayList<>();
        for (String extId : externalIds) {
            Long wsId = Long.parseLong(extId);
            List<PlayerSeasonStats> groupStats = statsGrouped.getOrDefault(wsId, List.of());
            if (groupStats.isEmpty()) continue;

            PlayerSeasonStats best = groupStats.stream()
                .max(Comparator.comparingInt(s -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0))
                .orElse(groupStats.get(0));

            List<String> teamNames = groupStats.stream()
                .map(PlayerSeasonStats::getTeamName)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

            int totalMinutes = groupStats.stream()
                .mapToInt(s -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0)
                .sum();

            List<PlayerMatchReview> revs = reviewsGrouped.get(extId);
            List<PlayerMatchReview> extReviews = revs != null ? new ArrayList<>(revs) : new ArrayList<>();
            extReviews.sort(Comparator.comparingDouble((PlayerMatchReview r) -> r.getScore() != null ? r.getScore() : 0.0).reversed());

            List<com.furbo.adapter.dto.ReviewCandidateDTO> candidateDTOs = new ArrayList<>();
            for (PlayerMatchReview r : extReviews) {
                Player p = playersMap.get(r.getCandidatePlayerId());
                if (p == null) continue;
                String linkedWs = playerToWsIdMap.get(p.getId());
                candidateDTOs.add(new com.furbo.adapter.dto.ReviewCandidateDTO(
                    p.getId(),
                    p.getName(),
                    p.getTeam(),
                    p.getPosition(),
                    p.getLeague(),
                    p.getNationality(),
                    r.getScore() != null ? r.getScore() : 0.0,
                    r.getReason(),
                    linkedWs
                ));
            }

            content.add(new com.furbo.adapter.dto.ReviewItemGroupDTO(
                wsId,
                best.getName(),
                teamNames,
                best.getTournamentName(),
                best.getPositionText(),
                best.getAge(),
                best.getHeight(),
                best.getWeight(),
                totalMinutes,
                candidateDTOs
            ));
        }

        return new com.furbo.adapter.dto.PagedReviewItemsDTO(content, normPage, normSize, totalElements, totalPages);
    }

    @Transactional(readOnly = true)
    public com.furbo.adapter.dto.UnmatchedCandidatesResponseDTO getUnmatchedCandidates(Long whoscoredId, String name, int size) {
        int normSize = Math.min(Math.max(size, 1), 200);
        validateStatsExist(whoscoredId);

        List<PlayerSeasonStats> stats = statsRepository.findByWhoscoredPlayerId(whoscoredId);
        PlayerSeasonStats best = stats.stream()
            .max(Comparator.comparingInt(s -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0))
            .orElse(stats.get(0));

        List<String> teamNames = stats.stream()
            .map(PlayerSeasonStats::getTeamName)
            .filter(java.util.Objects::nonNull)
            .distinct()
            .toList();

        int totalMinutes = stats.stream()
            .mapToInt(s -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0)
            .sum();

        List<com.furbo.adapter.dto.WhoscoredRowDTO> rows = stats.stream()
            .map(s -> new com.furbo.adapter.dto.WhoscoredRowDTO(
                s.getTeamName(),
                s.getTournamentName(),
                s.getSeasonName(),
                s.getMinsPlayed() != null ? s.getMinsPlayed() : 0
            ))
            .toList();

        com.furbo.adapter.dto.WhoscoredPlayerSummaryDTO playerSummary = new com.furbo.adapter.dto.WhoscoredPlayerSummaryDTO(
            whoscoredId,
            best.getName(),
            teamNames,
            best.getTournamentName(),
            best.getPositionText(),
            best.getAge(),
            best.getHeight(),
            best.getWeight(),
            totalMinutes,
            rows
        );

        Set<Long> linkedPlayerIds = sourceRepository.findAllBySource("WHOSCORED").stream()
            .map(PlayerSourceId::getPlayerId)
            .collect(java.util.stream.Collectors.toSet());

        List<Player> allPlayers = playerRepository.findAll();
        List<Player> pool = new ArrayList<>();

        if (name == null || name.isBlank()) {
            MatchContext ctx = matcherService.buildContext();
            Set<String> normWsTeams = teamNames.stream()
                .map(NameNormalizer::normalizeTeam)
                .collect(java.util.stream.Collectors.toSet());

            for (Player p : allPlayers) {
                if (linkedPlayerIds.contains(p.getId())) continue;
                String pTeamNorm = NameNormalizer.normalizeTeam(p.getTeam());
                boolean matches = false;
                for (String wsTeamNorm : normWsTeams) {
                    String mapped = ctx.getTeamMap() != null ? ctx.getTeamMap().get(wsTeamNorm) : null;
                    if ((mapped != null && pTeamNorm.equals(mapped)) || 
                        (pTeamNorm.contains(wsTeamNorm) || wsTeamNorm.contains(pTeamNorm))) {
                        matches = true;
                        break;
                    }
                }
                if (matches) {
                    pool.add(p);
                }
            }
        } else {
            String normSearch = NameNormalizer.normalizeName(name);
            for (Player p : allPlayers) {
                if (linkedPlayerIds.contains(p.getId())) continue;
                if (NameNormalizer.normalizeName(p.getName()).contains(normSearch)) {
                    pool.add(p);
                }
            }
        }

        record ScoredItem(Player player, Double score, String rule) {}
        List<ScoredItem> scoredItems = new ArrayList<>();

        for (Player p : pool) {
            NameSimilarity.Result res = NameSimilarity.evaluate(best.getName(), best.getFirstName(), best.getLastName(), best.getPositionText(), p);
            Double sc = res != null ? res.score() : null;
            String rl = res != null ? res.rule() : null;
            scoredItems.add(new ScoredItem(p, sc, rl));
        }

        scoredItems.sort((a, b) -> {
            if (a.score() != null && b.score() != null) {
                int cmp = Double.compare(b.score(), a.score());
                if (cmp != 0) return cmp;
            } else if (a.score() != null) {
                return -1;
            } else if (b.score() != null) {
                return 1;
            }
            return a.player().getName().compareToIgnoreCase(b.player().getName());
        });

        if (scoredItems.size() > normSize) {
            scoredItems = scoredItems.subList(0, normSize);
        }

        List<com.furbo.adapter.dto.UnmatchedCandidateItemDTO> candidateItems = scoredItems.stream()
            .map(si -> new com.furbo.adapter.dto.UnmatchedCandidateItemDTO(
                si.player().getId(),
                si.player().getName(),
                si.player().getTeam(),
                si.player().getPosition(),
                si.player().getLeague(),
                si.player().getNationality(),
                si.score(),
                si.rule()
            ))
            .toList();

        return new com.furbo.adapter.dto.UnmatchedCandidatesResponseDTO(playerSummary, candidateItems);
    }

    @Transactional(readOnly = true)
    public com.furbo.adapter.dto.PagedToConfirmDTO getToConfirmLinks(int page, int size) {
        int normSize = Math.min(Math.max(size, 1), 200);
        int normPage = Math.max(page, 0);

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(normPage, normSize);
        org.springframework.data.domain.Page<PlayerSourceId> pageLinks = sourceRepository.findToConfirmPaginated(pageable);
        List<PlayerSourceId> links = pageLinks.getContent();
        long totalElements = pageLinks.getTotalElements();
        int totalPages = pageLinks.getTotalPages();

        if (links.isEmpty()) {
            return new com.furbo.adapter.dto.PagedToConfirmDTO(List.of(), normPage, normSize, totalElements, totalPages);
        }

        List<Long> wsIds = links.stream().map(s -> Long.parseLong(s.getExternalId())).toList();
        List<Long> playerIds = links.stream().map(PlayerSourceId::getPlayerId).toList();

        List<PlayerSeasonStats> allStats = statsRepository.findByWhoscoredPlayerIdIn(wsIds);
        Map<Long, List<PlayerSeasonStats>> statsGrouped = allStats.stream()
            .collect(Collectors.groupingBy(PlayerSeasonStats::getWhoscoredPlayerId));

        Map<Long, Player> playersMap = playerRepository.findAllById(playerIds).stream()
            .collect(Collectors.toMap(Player::getId, p -> p, (p1, p2) -> p1));

        List<com.furbo.adapter.dto.ToConfirmItemDTO> content = new ArrayList<>();
        for (PlayerSourceId s : links) {
            Long wsId = Long.parseLong(s.getExternalId());
            List<PlayerSeasonStats> stats = statsGrouped.getOrDefault(wsId, List.of());
            PlayerSeasonStats best = stats.stream()
                .max(Comparator.comparingInt(st -> st.getMinsPlayed() != null ? st.getMinsPlayed() : 0))
                .orElse(stats.isEmpty() ? null : stats.get(0));

            List<String> teamNames = stats.stream()
                .map(PlayerSeasonStats::getTeamName)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();

            int totalMinutes = stats.stream()
                .mapToInt(st -> st.getMinsPlayed() != null ? st.getMinsPlayed() : 0)
                .sum();

            Player p = playersMap.get(s.getPlayerId());

            content.add(new com.furbo.adapter.dto.ToConfirmItemDTO(
                wsId,
                best != null ? best.getName() : "Unknown",
                teamNames,
                best != null ? best.getAge() : null,
                best != null ? best.getHeight() : null,
                best != null ? best.getWeight() : null,
                totalMinutes,
                s.getPlayerId(),
                p != null ? p.getName() : null,
                p != null ? p.getTeam() : null,
                p != null ? p.getPosition() : null,
                s.getMatchMethod(),
                s.getConfidence(),
                s.getCreatedAt()
            ));
        }

        return new com.furbo.adapter.dto.PagedToConfirmDTO(content, normPage, normSize, totalElements, totalPages);
    }
}
