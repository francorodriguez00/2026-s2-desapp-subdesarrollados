package com.furbo.service.matching;

import com.furbo.model.Player;
import com.furbo.repository.PlayerRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayerMatcherService {

    private final PlayerRepository playerRepository;
    private final FurboMatchingProperties props;

    public PlayerMatcherService(PlayerRepository playerRepository, FurboMatchingProperties props) {
        this.playerRepository = playerRepository;
        this.props = props;
    }

    public MatchContext buildContext() {
        List<Player> allPlayers = playerRepository.findAll();
        Map<String, List<Player>> byName = allPlayers.stream()
            .collect(Collectors.groupingBy(p -> NameNormalizer.normalizeName(p.getName())));
        Map<String, List<Player>> byTeam = allPlayers.stream()
            .collect(Collectors.groupingBy(p -> NameNormalizer.normalizeTeam(p.getTeam())));
        Map<Long, Player> byId = allPlayers.stream()
            .collect(Collectors.toMap(Player::getId, p -> p, (p1, p2) -> p1));
        
        return new MatchContext(byName, byTeam, byId, new HashMap<>(), new HashMap<>());
    }

    public MatchOutcome match(WhoScoredPlayerRef ref, MatchContext ctx) {
        String name = NameNormalizer.normalizeName(ref.name());
        List<Player> candidates = ctx.getPlayersByName().get(name);
        
        if (candidates != null && !candidates.isEmpty()) {
            if (candidates.size() == 1) {
                return new MatchOutcome.Linked(candidates.get(0).getId(), "EXACT_NAME", 1.0);
            }
            String team = ref.teamNames() != null && !ref.teamNames().isEmpty() ? NameNormalizer.normalizeTeam(ref.teamNames().get(0)) : "";
            List<Player> teamMatches = candidates.stream()
                .filter(p -> NameNormalizer.normalizeTeam(p.getTeam()).contains(team))
                .toList();
            if (teamMatches.size() == 1) {
                return new MatchOutcome.Linked(teamMatches.get(0).getId(), "EXACT_NAME_TEAM", 1.0);
            }
            return new MatchOutcome.Review(candidates.stream().map(p -> new MatchOutcome.Review.Candidate(p.getId(), 0.9, "HOMONYM")).toList());
        }
        return new MatchOutcome.Unmatched();
    }
}
