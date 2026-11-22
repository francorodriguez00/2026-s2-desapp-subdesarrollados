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

    public record ProcessOutcome<T>(T entry, LinkOutcome outcome) {}

    public static <T> java.util.List<ProcessOutcome<T>> executeTwoPasses(
        java.util.List<T> entries,
        java.util.function.Function<T, String> wsIdGetter,
        java.util.function.Function<T, WhoScoredPlayerRef> refGetter,
        java.util.function.Function<T, Long> existingGetter,
        java.util.function.Function<T, Integer> minsGetter,
        MatchContext ctx,
        PlayerLinkResolver resolver,
        java.util.Comparator<T> pass1Comparator
    ) {
        java.util.List<T> pass1Entries = new java.util.ArrayList<>(entries);
        if (pass1Comparator != null) {
            pass1Entries.sort(pass1Comparator);
        }

        java.util.List<ProcessOutcome<T>> results = new java.util.ArrayList<>();
        java.util.List<T> unmatchedForPass2 = new java.util.ArrayList<>();

        // Pass 1 (allowFuzzy = false)
        for (T entry : pass1Entries) {
            String wsId = wsIdGetter.apply(entry);
            WhoScoredPlayerRef ref = refGetter.apply(entry);
            Long existing = existingGetter.apply(entry);

            LinkOutcome outcome = resolver.resolve(wsId, ref, existing, ctx, false);
            if (outcome.getOutcome() == LinkOutcome.Type.UNMATCHED) {
                unmatchedForPass2.add(entry);
            } else {
                results.add(new ProcessOutcome<>(entry, outcome));
            }
        }

        // Sort Pass 2 by total minutes desc, wsId asc
        unmatchedForPass2.sort((e1, e2) -> {
            int m1 = minsGetter.apply(e1);
            int m2 = minsGetter.apply(e2);
            if (m1 != m2) {
                return Integer.compare(m2, m1);
            }
            return Long.compare(Long.parseLong(wsIdGetter.apply(e1)), Long.parseLong(wsIdGetter.apply(e2)));
        });

        // Pass 2 (allowFuzzy = true)
        for (T entry : unmatchedForPass2) {
            String wsId = wsIdGetter.apply(entry);
            WhoScoredPlayerRef ref = refGetter.apply(entry);
            Long existing = existingGetter.apply(entry);

            LinkOutcome outcome = resolver.resolve(wsId, ref, existing, ctx, true);
            results.add(new ProcessOutcome<>(entry, outcome));
        }

        return results;
    }

    public LinkOutcome resolve(String wsId, WhoScoredPlayerRef ref, Long existingInStats, MatchContext ctx) {
        return resolve(wsId, ref, existingInStats, ctx, true);
    }

    public LinkOutcome resolve(String wsId, WhoScoredPlayerRef ref, Long existingInStats, MatchContext ctx, boolean allowFuzzy) {
        Optional<PlayerSourceId> existingLink = persister.findExisting("WHOSCORED", wsId);
        if (existingLink.isPresent()) {
            persister.deleteReviews("WHOSCORED", wsId);
            return LinkOutcome.builder().playerId(existingLink.get().getPlayerId()).outcome(LinkOutcome.Type.ALREADY_LINKED).build();
        }

        if (existingInStats != null) {
            LinkResult res = persister.link(wsId, existingInStats, "LEGACY_EXACT_NAME", 0.90, false);
            if (res == LinkResult.CONFLICT) {
                return LinkOutcome.builder().outcome(LinkOutcome.Type.CONFLICT).disputedPlayerId(existingInStats).build();
            }
            if (res == LinkResult.CREATED) {
                ctx.getLinkedPlayerIds().add(existingInStats);
            }
            persister.deleteReviews("WHOSCORED", wsId);
            return LinkOutcome.builder().playerId(existingInStats).outcome(LinkOutcome.Type.LEGACY_CREATED).build();
        }

        MatchOutcome outcome = matcherService.matchExact(ref, ctx);
        if (outcome instanceof MatchOutcome.Unmatched && allowFuzzy) {
            outcome = matcherService.matchFuzzy(ref, ctx);
        }

        if (outcome instanceof MatchOutcome.Linked linked) {
            boolean needsReview = "FUZZY_TEAM".equals(linked.method());
            LinkResult res = persister.link(wsId, linked.playerId(), linked.method(), linked.confidence(), needsReview);
            if (res == LinkResult.CONFLICT) {
                 return LinkOutcome.builder().outcome(LinkOutcome.Type.CONFLICT).disputedPlayerId(linked.playerId()).build();
            }
            if (res == LinkResult.CREATED) {
                ctx.getLinkedPlayerIds().add(linked.playerId());
            }
            LinkOutcome.Type type = switch (linked.method()) {
                case "EXACT_NAME" -> LinkOutcome.Type.EXACT_NAME;
                case "EXACT_NAME_TEAM" -> LinkOutcome.Type.EXACT_TEAM;
                default -> LinkOutcome.Type.FUZZY_TEAM;
            };
            persister.deleteReviews("WHOSCORED", wsId);
            return LinkOutcome.builder().playerId(linked.playerId()).outcome(type).method(linked.method()).confidence(linked.confidence()).build();
        } else if (outcome instanceof MatchOutcome.Review review) {
            persister.saveReviews("WHOSCORED", wsId, review.candidates());
            return LinkOutcome.builder().outcome(LinkOutcome.Type.REVIEW).build();
        }

        persister.deleteReviews("WHOSCORED", wsId);
        return LinkOutcome.builder().outcome(LinkOutcome.Type.UNMATCHED).build();
    }

    public Optional<PlayerSourceId> tryFindExistingByPlayerId(Long playerId) {
        return persister.findExistingByPlayerId(playerId);
    }
}
