package com.furbo.service.matching;

import com.furbo.model.Player;
import com.furbo.model.PlayerSourceId;
import com.furbo.repository.PlayerRepository;
import com.furbo.repository.PlayerSourceIdRepository;
import com.furbo.repository.PlayerSeasonStatsRepository;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlayerMatcherService {

    private final PlayerRepository playerRepository;
    private final PlayerSourceIdRepository sourceRepository;
    private final PlayerSeasonStatsRepository statsRepository;
    private final FurboMatchingProperties props;
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(PlayerMatcherService.class);

    public PlayerMatcherService(PlayerRepository playerRepository, 
                                PlayerSourceIdRepository sourceRepository,
                                PlayerSeasonStatsRepository statsRepository,
                                FurboMatchingProperties props) {
        this.playerRepository = playerRepository;
        this.sourceRepository = sourceRepository;
        this.statsRepository = statsRepository;
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

        Set<Long> linkedPlayerIds = sourceRepository.findAllBySource("WHOSCORED").stream()
            .map(PlayerSourceId::getPlayerId)
            .collect(Collectors.toSet());

        Map<String, String> teamMap = new HashMap<>();
        try {
            List<Object[]> rawTeamVotes = sourceRepository.findTeamVotesNative();
            Map<String, Map<String, Long>> teamVoteCounts = new HashMap<>();
            Map<String, Long> teamTotalVotes = new HashMap<>();

            for (Object[] row : rawTeamVotes) {
                String wsTeam = (String) row[0];
                String playerTeam = (String) row[1];
                Number countNum = (Number) row[2];
                if (wsTeam == null || playerTeam == null || countNum == null) continue;
                long count = countNum.longValue();

                String normWsTeam = NameNormalizer.normalizeTeam(wsTeam);
                String normPlayerTeam = NameNormalizer.normalizeTeam(playerTeam);

                teamVoteCounts.computeIfAbsent(normWsTeam, k -> new HashMap<>())
                    .merge(normPlayerTeam, count, Long::sum);
                teamTotalVotes.merge(normWsTeam, count, Long::sum);
            }

            int minVotes = props.getTeamMapMinVotes();
            double minShare = props.getTeamMapMinShare();

            for (Map.Entry<String, Map<String, Long>> entry : teamVoteCounts.entrySet()) {
                String wsTeam = entry.getKey();
                long total = teamTotalVotes.getOrDefault(wsTeam, 0L);
                if (total < minVotes) continue;

                for (Map.Entry<String, Long> pEntry : entry.getValue().entrySet()) {
                    String playerTeam = pEntry.getKey();
                    long votes = pEntry.getValue();
                    double share = (double) votes / total;
                    if (share >= minShare) {
                        teamMap.put(wsTeam, playerTeam);
                        break;
                    }
                }
            }
            log.info("teamMap aprendido con {} entradas.", teamMap.size());
        } catch (Exception e) {
            log.error("Error aprendiendo teamMap", e);
        }

        Map<Integer, String> tournamentLeagueMap = new HashMap<>();
        try {
            List<Object[]> rawTournVotes = statsRepository.findTournamentLeagueVotesNative();
            Map<Integer, Map<String, Long>> tournVoteCounts = new HashMap<>();
            Map<Integer, Long> tournTotalVotes = new HashMap<>();

            for (Object[] row : rawTournVotes) {
                Integer tournId = row[0] instanceof Number ? ((Number) row[0]).intValue() : null;
                String league = (String) row[1];
                Number countNum = (Number) row[2];
                if (tournId == null || league == null || countNum == null) continue;
                long count = countNum.longValue();

                tournVoteCounts.computeIfAbsent(tournId, k -> new HashMap<>())
                    .merge(league, count, Long::sum);
                tournTotalVotes.merge(tournId, count, Long::sum);
            }

            int minVotes = props.getTeamMapMinVotes();
            double minShare = props.getTeamMapMinShare();

            for (Map.Entry<Integer, Map<String, Long>> entry : tournVoteCounts.entrySet()) {
                Integer tournId = entry.getKey();
                long total = tournTotalVotes.getOrDefault(tournId, 0L);
                if (total < minVotes) continue;

                for (Map.Entry<String, Long> pEntry : entry.getValue().entrySet()) {
                    String league = pEntry.getKey();
                    long votes = pEntry.getValue();
                    double share = (double) votes / total;
                    if (share >= minShare) {
                        tournamentLeagueMap.put(tournId, league);
                        break;
                    }
                }
            }
            log.info("tournamentLeagueMap aprendido con {} entradas.", tournamentLeagueMap.size());
        } catch (Exception e) {
            log.error("Error aprendiendo tournamentLeagueMap", e);
        }

        return new MatchContext(byName, byTeam, byId, teamMap, tournamentLeagueMap, linkedPlayerIds);
    }

    public MatchOutcome match(WhoScoredPlayerRef ref, MatchContext ctx) {
        return matchExact(ref, ctx);
    }

    public MatchOutcome matchExact(WhoScoredPlayerRef ref, MatchContext ctx) {
        String name = NameNormalizer.normalizeName(ref.name());
        List<Player> candidates = ctx.getPlayersByName().get(name);
        
        if (candidates != null && !candidates.isEmpty()) {
            List<Player> unlinked = candidates.stream()
                .filter(p -> !ctx.getLinkedPlayerIds().contains(p.getId()))
                .toList();

            if (unlinked.size() == 1) {
                return new MatchOutcome.Linked(unlinked.get(0).getId(), "EXACT_NAME", 1.0);
            }
            if (candidates.size() == 1) {
                return new MatchOutcome.Linked(candidates.get(0).getId(), "EXACT_NAME", 1.0);
            }
            String team = ref.teamNames() != null && !ref.teamNames().isEmpty() ? NameNormalizer.normalizeTeam(ref.teamNames().get(0)) : "";
            List<Player> teamMatches = unlinked.stream()
                .filter(p -> NameNormalizer.normalizeTeam(p.getTeam()).contains(team) || team.contains(NameNormalizer.normalizeTeam(p.getTeam())))
                .toList();
            if (teamMatches.size() == 1) {
                return new MatchOutcome.Linked(teamMatches.get(0).getId(), "EXACT_NAME_TEAM", 1.0);
            }
            return new MatchOutcome.Review(unlinked.stream().map(p -> new MatchOutcome.Review.Candidate(p.getId(), 0.9, "HOMONYM")).toList());
        }
        return new MatchOutcome.Unmatched();
    }

    public MatchOutcome matchFuzzy(WhoScoredPlayerRef ref, MatchContext ctx) {
        List<Player> pool = new ArrayList<>();
        boolean fromTeamMap = false;

        if (ref.teamNames() != null && !ref.teamNames().isEmpty() && ctx.getTeamMap() != null) {
            for (String wsTeam : ref.teamNames()) {
                String normWsTeam = NameNormalizer.normalizeTeam(wsTeam);
                String mappedTeam = ctx.getTeamMap().get(normWsTeam);
                if (mappedTeam != null && ctx.getPlayersByTeam() != null) {
                    List<Player> teamPlayers = ctx.getPlayersByTeam().get(mappedTeam);
                    if (teamPlayers != null) {
                        for (Player p : teamPlayers) {
                            if (!ctx.getLinkedPlayerIds().contains(p.getId()) && !pool.contains(p)) {
                                pool.add(p);
                                fromTeamMap = true;
                            }
                        }
                    }
                }
            }
        }

        if (pool.isEmpty() && ref.tournamentIds() != null && !ref.tournamentIds().isEmpty() && ctx.getTournamentLeagueMap() != null) {
            for (Integer tournId : ref.tournamentIds()) {
                String league = ctx.getTournamentLeagueMap().get(tournId);
                if (league != null) {
                    List<Player> leaguePlayers = ctx.getPlayersByName().values().stream()
                        .flatMap(List::stream)
                        .filter(p -> league.equalsIgnoreCase(p.getLeague()) && !ctx.getLinkedPlayerIds().contains(p.getId()))
                        .toList();
                    for (Player p : leaguePlayers) {
                        if (!pool.contains(p)) {
                            pool.add(p);
                        }
                    }
                }
            }
        }

        if (pool.isEmpty()) {
            return new MatchOutcome.Unmatched();
        }

        record ScoredCandidate(Player player, NameSimilarity.Result res) {}
        List<ScoredCandidate> scored = new ArrayList<>();

        for (Player p : pool) {
            NameSimilarity.Result res = NameSimilarity.evaluate(ref.name(), ref.firstName(), ref.lastName(), p);
            if (res != null && res.score() >= props.getReviewMinScore()) {
                scored.add(new ScoredCandidate(p, res));
            }
        }

        if (scored.isEmpty()) {
            return new MatchOutcome.Unmatched();
        }

        scored.sort((a, b) -> Double.compare(b.res().score(), a.res().score()));

        if (fromTeamMap) {
            double autoMin = props.getAutoLinkMinScore();
            List<ScoredCandidate> strong = scored.stream()
                .filter(sc -> sc.res().score() >= autoMin && isStrongRule(sc.res().rule()))
                .toList();

            if (strong.size() == 1) {
                ScoredCandidate sc = strong.get(0);
                return new MatchOutcome.Linked(sc.player().getId(), "FUZZY_TEAM", sc.res().score());
            }
        }

        int maxReview = props.getMaxReviewCandidates();
        List<MatchOutcome.Review.Candidate> candidates = scored.stream()
            .limit(maxReview)
            .map(sc -> new MatchOutcome.Review.Candidate(sc.player().getId(), sc.res().score(), sc.res().rule()))
            .toList();

        return new MatchOutcome.Review(candidates);
    }

    private boolean isStrongRule(String rule) {
        return "CONTAINED".equals(rule) || "ABBREVIATION".equals(rule) || "TYPO".equals(rule);
    }
}
