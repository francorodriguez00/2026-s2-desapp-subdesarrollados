package com.furbo.service.matching;

import com.furbo.model.*;
import com.furbo.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class PlayerLinkResolver {

    private final PlayerMatchingPersister persister;
    private final PlayerMatcherService matcherService;

    public PlayerLinkResolver(PlayerMatchingPersister persister, PlayerMatcherService matcherService) {
        this.persister = persister;
        this.matcherService = matcherService;
    }

    public LinkOutcome resolve(String wsId, WhoScoredPlayerRef ref, Long existingInStats, MatchContext ctx) {
        Optional<PlayerSourceId> existingLink = persister.findExisting("WHOSCORED", wsId);
        if (existingLink.isPresent()) {
            return LinkOutcome.builder().playerId(existingLink.get().getPlayerId()).outcome(LinkOutcome.Type.ALREADY_LINKED).build();
        }

        if (existingInStats != null) {
            LinkResult res = persister.link(wsId, existingInStats, "LEGACY_EXACT_NAME", 0.90);
            if (res == LinkResult.CONFLICT) {
                return LinkOutcome.builder().outcome(LinkOutcome.Type.CONFLICT).disputedPlayerId(existingInStats).build();
            }
            return LinkOutcome.builder().playerId(existingInStats).outcome(LinkOutcome.Type.LEGACY_CREATED).build();
        }

        MatchOutcome outcome = matcherService.match(ref, ctx);
        if (outcome instanceof MatchOutcome.Linked linked) {
            LinkResult res = persister.link(wsId, linked.playerId(), linked.method(), linked.confidence());
            if (res == LinkResult.CONFLICT) {
                 return LinkOutcome.builder().outcome(LinkOutcome.Type.CONFLICT).disputedPlayerId(linked.playerId()).build();
            }
            LinkOutcome.Type type = switch (linked.method()) {
                case "EXACT_NAME" -> LinkOutcome.Type.EXACT_NAME;
                case "EXACT_NAME_TEAM" -> LinkOutcome.Type.EXACT_TEAM;
                default -> LinkOutcome.Type.FUZZY_TEAM;
            };
            return LinkOutcome.builder().playerId(linked.playerId()).outcome(type).method(linked.method()).confidence(linked.confidence()).build();
        } else if (outcome instanceof MatchOutcome.Review) {
            return LinkOutcome.builder().outcome(LinkOutcome.Type.REVIEW).build();
        }

        return LinkOutcome.builder().outcome(LinkOutcome.Type.UNMATCHED).build();
    }

    public Optional<PlayerSourceId> tryFindExistingByPlayerId(Long playerId) {
        return persister.findExistingByPlayerId(playerId);
    }
}
