package com.furbo.service.matching;

import com.furbo.model.*;
import com.furbo.repository.*;
import com.furbo.service.matching.*;
import com.furbo.adapter.dto.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
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
        List<PlayerSeasonStats> allStats = statsRepository.findAll();
        Map<Long, List<PlayerSeasonStats>> grouped = allStats.stream()
            .filter(s -> s.getWhoscoredPlayerId() != null)
            .collect(Collectors.groupingBy(PlayerSeasonStats::getWhoscoredPlayerId));
        
        MatchContext ctx = matcherService.buildContext();
        int alreadyLinked = 0, legacyCreated = 0, conflicts = 0, reviewPending = 0, unmatched = 0;

        for (Map.Entry<Long, List<PlayerSeasonStats>> entry : grouped.entrySet()) {
            String wsId = String.valueOf(entry.getKey());
            List<PlayerSeasonStats> rows = entry.getValue();
            PlayerSeasonStats best = rows.stream()
                .max(Comparator.comparingInt(s -> s.getMinsPlayed() != null ? s.getMinsPlayed() : 0))
                .orElse(rows.get(0));

            Long existingInStats = rows.stream().map(PlayerSeasonStats::getPlayerId).filter(Objects::nonNull).findFirst().orElse(null);
            WhoScoredPlayerRef ref = new WhoScoredPlayerRef(wsId, best.getName(), best.getFirstName(), best.getLastName(),
                        rows.stream().map(PlayerSeasonStats::getTeamName).distinct().toList(), 
                        rows.stream().map(PlayerSeasonStats::getTournamentId).distinct().toList(), best.getPositionText());

            LinkOutcome outcome = resolver.resolve(wsId, ref, existingInStats, ctx);
            
        // TODO: Adaptar a métodos reales en repositorio
        // statsRepository.updatePlayerIdForWhoscoredId(outcome.getPlayerId(), entry.getKey());
        // statsRepository.nullifyPlayerIdForWhoscoredIdIfConflict(entry.getKey(), outcome.getDisputedPlayerId());
        
        if (outcome.getPlayerId() != null) {
            rows.forEach(s -> s.setPlayerId(outcome.getPlayerId()));
            statsRepository.saveAll(rows);
        } else if (outcome.getOutcome() == LinkOutcome.Type.CONFLICT) {
            conflicts++;
            rows.forEach(s -> s.setPlayerId(null));
            statsRepository.saveAll(rows);
        }
        }
        return new RelinkSummaryDTO(alreadyLinked, legacyCreated, 0, 0, 0, conflicts, reviewPending, unmatched, new ArrayList<>(), new ArrayList<>());
    }
}
