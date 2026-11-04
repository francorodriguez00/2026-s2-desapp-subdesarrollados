package com.furbo.service.matching;

import com.furbo.model.*;
import com.furbo.repository.*;
import com.furbo.adapter.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class PlayerLinkService {

    private final PlayerSeasonStatsRepository statsRepository;
    private final PlayerSourceIdRepository sourceRepository;
    private final PlayerLinkResolver resolver;
    private final PlayerMatcherService matcherService;

    public PlayerLinkService(PlayerSeasonStatsRepository statsRepository, 
                             PlayerSourceIdRepository sourceRepository,
                             PlayerLinkResolver resolver, 
                             PlayerMatcherService matcherService) {
        this.statsRepository = statsRepository;
        this.sourceRepository = sourceRepository;
        this.resolver = resolver;
        this.matcherService = matcherService;
    }

    @Transactional
    public RelinkSummaryDTO relinkAll() {
        MatchContext ctx = matcherService.buildContext();
        int alreadyLinked = 0, legacyCreated = 0, exactName = 0, exactTeam = 0, fuzzyTeam = 0, conflicts = 0, reviewPending = 0, unmatched = 0;

        List<ConflictDTO> conflictDTOs = new ArrayList<>();
        List<PlayerSeasonStats> trackedRows = statsRepository.findAll();
        Map<Long, List<PlayerSeasonStats>> groupedForStats = trackedRows.stream()
            .filter(s -> s.getWhoscoredPlayerId() != null)
            .collect(Collectors.groupingBy(PlayerSeasonStats::getWhoscoredPlayerId));

        List<Map.Entry<Long, List<PlayerSeasonStats>>> sortedEntries = new ArrayList<>(groupedForStats.entrySet());
        sortedEntries.sort((e1, e2) -> WhoscoredGroupComparator.compareStatsEntries(e1, e2, ctx));

        for (Map.Entry<Long, List<PlayerSeasonStats>> entry : sortedEntries) {
            Long wsIdLong = entry.getKey();
            String wsId = String.valueOf(wsIdLong);
            List<PlayerSeasonStats> rows = entry.getValue();
            PlayerSeasonStats best = rows.stream()
                .max(Comparator.comparingInt(s -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0))
                .orElse(rows.get(0));

            Long existingInStats = rows.stream().map(PlayerSeasonStats::getPlayerId).filter(Objects::nonNull).findFirst().orElse(null);
            WhoScoredPlayerRef ref = new WhoScoredPlayerRef(wsId, best.getName(), best.getFirstName(), best.getLastName(),
                        rows.stream().map(PlayerSeasonStats::getTeamName).distinct().toList(), 
                        rows.stream().map(PlayerSeasonStats::getTournamentId).distinct().toList(), best.getPositionText());

            LinkOutcome outcome = resolver.resolve(wsId, ref, existingInStats, ctx);
            
            switch (outcome.getOutcome()) {
                case ALREADY_LINKED -> alreadyLinked++;
                case LEGACY_CREATED -> legacyCreated++;
                case EXACT_NAME -> exactName++;
                case EXACT_TEAM -> exactTeam++;
                case FUZZY_TEAM -> fuzzyTeam++;
                case REVIEW -> reviewPending++;
                case UNMATCHED -> unmatched++;
                case CONFLICT -> {
                    conflicts++;
                    String ownerExtId = "";
                    Optional<PlayerSourceId> ownerSource = sourceRepository.findByPlayerIdAndSource(outcome.getDisputedPlayerId(), "WHOSCORED");
                    if (ownerSource.isPresent()) {
                        ownerExtId = ownerSource.get().getExternalId();
                    }
                    conflictDTOs.add(new ConflictDTO(
                        wsId,
                        best.getName(),
                        best.getTeamName(),
                        rows.stream().mapToInt(s -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0).sum(),
                        outcome.getDisputedPlayerId(),
                        ownerExtId
                    ));
                    statsRepository.clearPlayerIdForWhoscoredId(wsIdLong);
                }
            }

            if (outcome.getOutcome() != LinkOutcome.Type.CONFLICT && outcome.getPlayerId() != null) {
                boolean needsUpdate = rows.stream().anyMatch(s -> !Objects.equals(s.getPlayerId(), outcome.getPlayerId()));
                if (needsUpdate) {
                    statsRepository.setPlayerIdForWhoscoredId(wsIdLong, outcome.getPlayerId());
                }
            }
        }

        List<PlayerSeasonStats> updatedStats = statsRepository.findAll();
        List<UnmatchedPlayerDTO> unmatchedDTOs = updatedStats.stream()
            .filter(s -> s.getPlayerId() == null)
            .collect(Collectors.groupingBy(PlayerSeasonStats::getWhoscoredPlayerId))
            .entrySet().stream()
            .map(e -> {
                PlayerSeasonStats best = e.getValue().stream().max(Comparator.comparingInt(s -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0)).orElse(e.getValue().get(0));
                return new UnmatchedPlayerDTO(e.getKey(), best.getName(), best.getTeamName(), 
                    best.getTournamentName(), best.getPositionText(), 
                    e.getValue().stream().mapToInt(s -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0).sum());
            })
            .sorted(Comparator.comparingInt(UnmatchedPlayerDTO::totalMins).reversed())
            .limit(30)
            .toList();

        RelinkSummaryDTO summary = new RelinkSummaryDTO(
            alreadyLinked, legacyCreated, exactName, exactTeam, fuzzyTeam, conflicts, reviewPending, unmatched, unmatchedDTOs, conflictDTOs
        );

        log.info("Resumen relink: alreadyLinked={}, legacyCreated={}, exactName={}, exactTeam={}, fuzzyTeam={}, conflicts={}, reviewPending={}, unmatched={}, unmatchedPlayersCount={}, conflictsCount={}",
            alreadyLinked, legacyCreated, exactName, exactTeam, fuzzyTeam, conflicts, reviewPending, unmatched, unmatchedDTOs.size(), conflictDTOs.size());

        return summary;
    }
}
